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
import com.tuplejump.stargate.cassandra.CustomColumnFactory;
import com.tuplejump.stargate.cassandra.IndexEntryCollector;
import com.tuplejump.stargate.cassandra.RowScanner;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 */

public abstract class Aggregate implements Function {

    public static final String DEFAULT = "~__default__~";
    protected String field;
    protected String alias;
    protected String groupBy;
    protected boolean distinct = false;

    public Aggregate(@JsonProperty("field") String field, @JsonProperty("alias") String alias, @JsonProperty("distinct") boolean distinct, @JsonProperty("groupBy") String groupBy) {
        this.field = field;
        this.alias = alias == null ? getFunction() : alias;
        this.distinct = distinct;
        this.groupBy = groupBy;
    }

    public abstract String getFunction();

    @Override
    public boolean shouldLimit() {
        return false;
    }

    public Grouped values(RowScanner rowScanner, ColumnFamilyStore table) throws Exception {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        Grouped grouped = new Grouped(true);
        if (rowScanner.getCollector().canByPassRowFetch()) {
            byPassRowFetch(rowScanner, grouped);
        } else {
            while (rowScanner.hasNext()) {
                Row row = rowScanner.next();
                ColumnFamily cf = row.cf;
                Collection<Column> cols = cf.getSortedColumns();
                String group = DEFAULT;
                Object value = null;
                for (Column column : cols) {
                    String actualColumnName = Utils.getColumnNameStr(baseComparator, column.name());
                    AbstractType<?> valueValidator = table.metadata.getValueValidatorFromColumnName(column.name());
                    if (groupBy != null && groupBy.equalsIgnoreCase(actualColumnName)) {
                        group = valueValidator.getString(column.value());
                    }
                    if (field.equalsIgnoreCase(actualColumnName)) {
                        value = valueValidator.compose(column.value());
                    }
                }
                values(group, grouped).add(value);
            }
        }

        return grouped;
    }

    private void byPassRowFetch(RowScanner rowScanner, Grouped grouped) {
        Options options = rowScanner.getOptions();
        Iterator<IndexEntryCollector.IndexEntry> indexIterator = rowScanner.getCollector().docs().iterator();
        String field = getField();
        String groupBy = getGroupBy();
        AbstractType valueValidator = options.validators.get(getField());
        AbstractType groupValidator = groupBy == null ? null : options.validators.get(groupBy);
        while (indexIterator.hasNext()) {
            IndexEntryCollector.IndexEntry indexEntry = indexIterator.next();
            Object value = getValue(indexEntry, field, valueValidator, false);
            String group = groupBy == null ? DEFAULT : (String) getValue(indexEntry, groupBy, groupValidator, true);
            values(group, grouped).add(value);
        }
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

    private Collection<Object> values(String group, Grouped grouped) {
        Collection<Object> results = grouped.values(group);
        if (results == null) {
            if (distinct) {
                results = new TreeSet<>();
            } else {
                results = new ArrayList<>();
            }
            grouped.values(group, results);
        }
        return results;
    }


    protected boolean isNumber(CQL3Type cqlType) {
        if (cqlType == CQL3Type.Native.INT || cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT ||
                cqlType == CQL3Type.Native.COUNTER || cqlType == CQL3Type.Native.DECIMAL
                || cqlType == CQL3Type.Native.DOUBLE || cqlType == CQL3Type.Native.FLOAT)
            return true;
        return false;
    }

    public List<Row> singleRow(String valueStr, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) {
        ByteBuffer value = UTF8Type.instance.decompose("{'" + alias + "':" + valueStr + "}");
        Row row = customColumnFactory.getRowWithMetaColumn(table, currentIndex, value);
        return Collections.singletonList(row);
    }

    public boolean isDistinct() {
        return distinct;
    }

    public String getField() {
        return field != null ? field.toLowerCase() : null;
    }

    public String getAlias() {
        return alias;
    }

    public String getGroupBy() {
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
