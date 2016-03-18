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

import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.cassandra.ResultMapper;
import com.tuplejump.stargate.cassandra.RowFetcher;
import com.tuplejump.stargate.cassandra.SearchSupport;
import com.tuplejump.stargate.lucene.*;
import com.tuplejump.stargate.lucene.Properties;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
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
    String[] aggregateFields;
    int chunkSize = 1;
    ExecutableStatement[] groupByExpressions;
    String[] imports;
    boolean noScript;
    boolean[] simpleExpressions;
    Options options;
    Group group;
    protected String[] selection;


    public AggregateFunction(@JsonProperty("aggregates") AggregateFactory[] aggregates, @JsonProperty("distinct") boolean distinct, @JsonProperty("groupBy") String[] groupBy, @JsonProperty("chunkSize") Integer chunkSize, @JsonProperty("imports") String[] imports, @JsonProperty("noScript") boolean noScript) {
        this.aggregates = aggregates;
        this.distinct = distinct;
        this.groupBy = groupBy;
        this.imports = imports;
        if (chunkSize != null) this.chunkSize = chunkSize;
        this.noScript = noScript;
    }


    @Override
    public boolean shouldTryScoring() {
        return false;
    }

    public Map<String, Integer> getPositions() {
        return positions;
    }

    public boolean[] getSimpleExpressions() {
        return simpleExpressions;
    }

    @Override
    public List<Row> process(ResultMapper resultMapper, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        Options options = resultMapper.searchSupport.getOptions();
        if (aggregates.length == 1 && !aggregates[0].distinct && "count".equalsIgnoreCase(aggregates[0].getType()) && groupBy == null) {
            //this means it is a count-star. we can simply return the size of the index results
            Count count = new Count(aggregates[0], false);
            count.count = resultMapper.collector.docs().size();
            group.groups.put(new Tuple(options.nestedFields, Collections.EMPTY_MAP, simpleExpressions), count);
            Row row = resultMapper.tableMapper.getRowWithMetaColumn(group.toByteBuffer());
            return Collections.singletonList(row);
        }
        Tuple tuple = createTuple(options);
        if (resultMapper.collector.canByPassRowFetch()) {
            for (IndexEntryCollector.IndexEntry indexEntry : resultMapper.collector.docs()) {
                load(tuple, indexEntry);
                group.addTuple(tuple);
            }
        } else {
            RowFetcher rowFetcher = new RowFetcher(resultMapper);
            for (Row row : rowFetcher.fetchRows()) {
                resultMapper.tableMapper.load(positions, tuple, row);
                group.addTuple(tuple);
            }

        }
        Utils.SimpleTimer timer3 = Utils.getStartedTimer(SearchSupport.logger);
        ByteBuffer groupBuffer = group.toByteBuffer();
        timer3.endLogTime("Aggregation serialization  [" + group.groups.size() + "] results");

        Row row = resultMapper.tableMapper.getRowWithMetaColumn(groupBuffer);
        return Collections.singletonList(row);
    }

    @Override
    public boolean needsPaging() {
        return false;
    }

    public Tuple createTuple(Options options) {
        return new Tuple(options.nestedFields, positions, simpleExpressions);
    }

    @Override
    public void init(Options options) {
        this.options = options;
        int k = 0;
        positions = new HashMap<>();
        aggregateFields = new String[aggregates.length];
        for (AggregateFactory aggregateFactory : aggregates) {
            String field = aggregateFactory.getField();
            if (field != null) {
                aggregateFields[k] = field;
                positions.put(field, k++);
            }
        }

        ParserConfiguration parserConfig = getParserConfiguration();

        if (groupBy != null) {
            this.groupByExpressions = new ExecutableStatement[groupBy.length];
            this.simpleExpressions = new boolean[groupBy.length];
            groupByFields = new ArrayList<>();
            for (int i = 0; i < groupBy.length; i++) {
                String groupByField = groupBy[i];
                String groupByCol = getGroupBy(groupByField);
                boolean isSimpleExpression = options.types.containsKey(getColumnName(groupByCol));
                ParserContext parserContext = new ParserContext(parserConfig);
                groupByExpressions[i] = (ExecutableStatement) MVEL.compileExpression(groupByField, parserContext);
                if (isSimpleExpression) {
                    simpleExpressions[i] = true;
                    int pos = k++;
                    positions.put(groupByCol, pos);
                    groupByFields.add(groupByCol);
                } else {
                    simpleExpressions[i] = false;
                    Set<String> keys = parserContext.getInputs().keySet();
                    for (String key : keys) {
                        int pos = k++;
                        boolean canResolve = options.types.containsKey(getColumnName(key));
                        if (canResolve) {
                            String groupByColField = getGroupBy(key);
                            positions.put(key, pos);
                            groupByFields.add(groupByColField);
                        }
                    }
                }
            }

        }
        group = new Group(options, aggregates, groupBy, groupByExpressions);

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


    public static Type getLuceneType(Options options, String field) {
        String validatorFieldName = getColumnName(field);
        if (validatorFieldName == null) return null;
        return options.types.get(validatorFieldName);
    }


    public static String getColumnName(String field) {
        return field != null ? Constants.dotSplitter.split(field).iterator().next() : null;
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


    public void load(Tuple tuple, IndexEntryCollector.IndexEntry entry) {
        for (String field : positions.keySet()) {
            Type validator = options.types.get(field);
            load(tuple, entry, field, validator);
            if (validator == null) {
                Iterator<String> fieldNameParts = Constants.dotSplitter.split(field).iterator();
                String columnName = fieldNameParts.next();
                if (options.nestedFields.contains(columnName)) {
                    Properties columnProps = options.fields.get(columnName);
                    Properties fieldProps;
                    if (columnProps.getType() == Type.map) {
                        fieldProps = columnProps.getFields().get("_value");
                    } else {
                        fieldProps = columnProps.getFields().get(fieldNameParts.next());
                    }
                    load(tuple, entry, field, fieldProps.getType());
                }
            }
        }
    }

    private void load(Tuple tuple, IndexEntryCollector.IndexEntry entry, String field, Type validator) {
        if (validator != null) {
            if (validator.isNumeric()) {
                tuple.tuple[this.positions.get(field)] = entry.getNumber(field);
            } else if (validator == Type.date) {
                Number number = entry.getNumber(field);
                tuple.tuple[this.positions.get(field)] = new Date(number.longValue());
            } else {
                tuple.tuple[this.positions.get(field)] = entry.getString(field);
            }
        }
    }


    public Group getGroup() {
        return group;
    }
}
