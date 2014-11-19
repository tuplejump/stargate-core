/*
 * Copyright 2014, Tuplejump Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuplejump.stargate.lucene.query.function;

import com.tuplejump.stargate.Constants;
import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.cassandra.CustomColumnFactory;
import com.tuplejump.stargate.cassandra.IndexEntryCollector;
import com.tuplejump.stargate.cassandra.RowScanner;
import com.tuplejump.stargate.cassandra.SearchSupport;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.*;
import org.codehaus.jackson.annotate.JsonProperty;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 */

public class AggregateFunction implements Function {

    protected AggregateFactory[] aggregates;
    protected String[] groupBy;
    List<String> groupByFields;
    protected boolean distinct = false;
    Map<String, Integer> positions;
    Map<String, AbstractType> allValidators;
    String[] aggregateFields;
    int chunkSize = 1;
    ExecutableStatement[] groupByExpressions;
    String[] imports;
    boolean noScript;
    boolean[] simpleExpressions;


    public AggregateFunction(@JsonProperty("aggregates") AggregateFactory[] aggregates, @JsonProperty("distinct") boolean distinct, @JsonProperty("groupBy") String[] groupBy, @JsonProperty("chunkSize") Integer chunkSize, @JsonProperty("imports") String[] imports, @JsonProperty("noScript") boolean noScript) {
        this.aggregates = aggregates;
        this.distinct = distinct;
        this.groupBy = groupBy;
        this.imports = imports;
        if (chunkSize != null) this.chunkSize = chunkSize;
        this.noScript = noScript;
    }


    @Override
    public boolean shouldLimit() {
        return false;
    }

    @Override
    public List<Row> process(RowScanner rowScanner, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        Options options = rowScanner.getOptions();

        Group group = new Group(options, aggregates, groupBy, groupByExpressions);
        if (aggregates.length == 1 && !aggregates[0].distinct && "count".equalsIgnoreCase(aggregates[0].getType()) && groupBy == null) {
            //this means it is a count-star. we can simply return the size of the index results
            Count count = new Count(aggregates[0], null, false);
            count.count = rowScanner.getCollector().docs().size();
            group.groups.put(new Tuple(Collections.EMPTY_MAP, Collections.EMPTY_MAP, simpleExpressions), count);
            Row row = customColumnFactory.getRowWithMetaColumn(table, currentIndex, group.toByteBuffer());
            return Collections.singletonList(row);
        }
        Tuple tuple = new Tuple(positions, allValidators, simpleExpressions);
        if (rowScanner.getCollector().canByPassRowFetch()) {
            Iterator<IndexEntryCollector.IndexEntry> indexIterator = rowScanner.getCollector().docs().iterator();
            while (indexIterator.hasNext()) {
                IndexEntryCollector.IndexEntry indexEntry = indexIterator.next();
                tuple.load(indexEntry);
                group.addTuple(tuple);
            }
        } else {
            if (chunkSize == 1) {
                while (rowScanner.hasNext()) {
                    Row row = rowScanner.next();
                    tuple.load(row, table);
                    group.addTuple(tuple);
                }
            } else {
                List<Row> rows;
                while (rowScanner.hasNext()) {
                    rows = new ArrayList<>(chunkSize);
                    for (int i = 0; i < chunkSize; i++) {
                        if (!rowScanner.hasNext()) break;
                        rows.add(rowScanner.next());
                    }
                    for (Row row : rows) {
                        tuple.load(row, table);
                        group.addTuple(tuple);
                    }
                }
            }

        }
        Utils.SimpleTimer timer3 = Utils.getStartedTimer(SearchSupport.logger);
        ByteBuffer groupBuffer = group.toByteBuffer();
        timer3.endLogTime("Aggregation serialization  [" + group.groups.size() + "] results");

        Row row = customColumnFactory.getRowWithMetaColumn(table, currentIndex, groupBuffer);
        return Collections.singletonList(row);
    }

    @Override
    public void init(Options options) {
        int k = 0;
        positions = new HashMap<>();
        allValidators = new HashMap<>();
        aggregateFields = new String[aggregates.length];
        for (AggregateFactory aggregateFactory : aggregates) {
            String field = aggregateFactory.getField();
            aggregateFields[k] = field;
            positions.put(field, k++);
            allValidators.put(field, getValidator(options, field));
        }

        ParserConfiguration parserConfig = getParserConfiguration();

        if (groupBy != null) {
            this.groupByExpressions = new ExecutableStatement[groupBy.length];
            this.simpleExpressions = new boolean[groupBy.length];
            groupByFields = new ArrayList<>();
            for (int i = 0; i < groupBy.length; i++) {
                String groupByField = groupBy[i];
                String groupByCol = getGroupBy(groupByField);
                AbstractType groupValidator = getValidator(options, groupByCol);
                boolean isSimpleExpression = groupValidator != null;
                ParserContext parserContext = new ParserContext(parserConfig);
                groupByExpressions[i] = (ExecutableStatement) MVEL.compileExpression(groupByField, parserContext);
                if (isSimpleExpression) {
                    simpleExpressions[i] = true;
                    int pos = k++;
                    positions.put(groupByCol, pos);
                    allValidators.put(groupByCol, groupValidator);
                    allValidators.put(getColumnName(groupByCol), groupValidator);
                    groupByFields.add(groupByCol);
                } else {
                    simpleExpressions[i] = false;
                    Set<String> keys = parserContext.getInputs().keySet();
                    for (String key : keys) {
                        int pos = k++;
                        String groupByColField = getGroupBy(key);
                        AbstractType groupFValidator = getValidator(options, groupByColField);
                        positions.put(key, pos);
                        allValidators.put(groupByColField, groupFValidator);
                        groupByFields.add(groupByColField);
                    }
                }
            }

        }

    }

    private ParserConfiguration getParserConfiguration() {
        ParserConfiguration parserConfig = new ParserConfiguration();
        parserConfig.addPackageImport("java.util");
        parserConfig.addPackageImport("org.apache.commons.lang3");
        parserConfig.addPackageImport("org.joda.time");
        parserConfig.addImport(Math.class);
        if (imports != null) {
            for (String imported : imports)
                parserConfig.addPackageImport(imported);
        }
        return parserConfig;
    }

    public List<String> getGroupByFields() {
        return groupByFields;
    }

    public List<String> getAggregateFields() {
        if (aggregateFields == null) return null;
        List<String> aggFields = new ArrayList<>(aggregateFields.length);
        for (String field : aggregateFields) {
            if (field != null) aggFields.add(field);
        }
        return aggFields;
    }

    private static AbstractType getValidator(Options options, String field) {
        String validatorFieldName = getColumnName(field);
        if (validatorFieldName == null) return null;
        return options.validators.get(validatorFieldName);
    }

    public static AbstractType getFieldValidator(Options options, String field) {
        String validatorFieldName = getColumnName(field);
        if (validatorFieldName == null) return null;
        return getValueValidator(options.validators.get(validatorFieldName));
    }

    public static AbstractType getValueValidator(AbstractType abstractType) {
        if (abstractType instanceof CollectionType) {
            if (abstractType instanceof MapType) {
                MapType mapType = (MapType) abstractType;
                return mapType.valueComparator();
            } else if (abstractType instanceof SetType) {
                SetType setType = (SetType) abstractType;
                return setType.nameComparator();
            } else if (abstractType instanceof ListType) {
                ListType listType = (ListType) abstractType;
                return listType.valueComparator();
            }
        }
        return abstractType;
    }

    public static String getColumnName(String field) {
        return field != null ? Constants.dotSplitter.split(field).iterator().next() : null;
    }

    protected Object getValue(IndexEntryCollector.IndexEntry indexEntry, String field, AbstractType valueValidator, boolean asString) {
        if (isNumber(valueValidator.asCQL3Type())) {
            Number number = indexEntry.getNumber(field);
            return asString ? number.toString() : number;
        } else {
            ByteBuffer byteBuffer = indexEntry.getByteBuffer(field);
            return asString ? valueValidator.getString(byteBuffer) : valueValidator.compose(byteBuffer);
        }
    }


    protected boolean isNumber(CQL3Type cqlType) {
        if (cqlType == CQL3Type.Native.INT || cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT ||
                cqlType == CQL3Type.Native.COUNTER || cqlType == CQL3Type.Native.DECIMAL
                || cqlType == CQL3Type.Native.DOUBLE || cqlType == CQL3Type.Native.FLOAT)
            return true;
        return false;
    }


    public static String getGroupBy(String groupBy) {
        return groupBy != null ? groupBy.toLowerCase() : null;
    }

    public static class NumberComparator implements Comparator<Number> {

        public int compare(Number a, Number b) {
            return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
        }

        public static int compareNumbers(Number a, Number b) {
            return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
        }

    }
}
