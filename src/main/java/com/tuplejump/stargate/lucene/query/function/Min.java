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
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.*;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: satya
 */
public class Min extends Aggregate {

    protected boolean reverse = false;

    @JsonCreator
    public Min(@JsonProperty("field") String field, @JsonProperty("name") String name, @JsonProperty("groupBy") String groupBy) {
        super(field, name, true, groupBy);
    }

    public String getFunction() {
        return "min";
    }

    @Override
    public List<Row> process(RowScanner rowScanner, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        Grouped grouped = new Grouped(false);

        if (rowScanner.getCollector().canByPassRowFetch()) {
            return byPassRowFetch(rowScanner, customColumnFactory, table, currentIndex, grouped);

        } else {
            AbstractType valueValidator = null;
            while (rowScanner.hasNext()) {
                Row row = rowScanner.next();
                String group = DEFAULT;
                ByteBuffer colValue = null;
                Collection<Column> cols = row.cf.getSortedColumns();
                for (Column column : cols) {
                    String actualColumnName = Utils.getColumnNameStr(baseComparator, column.name());
                    ByteBuffer colValueToUse = column.value();
                    valueValidator = table.metadata.getValueValidatorFromColumnName(column.name());
                    if (valueValidator.isCollection()) {
                        CollectionType validator = (CollectionType) valueValidator;
                        AbstractType keyType = validator.nameComparator();
                        AbstractType valueType = validator.valueComparator();
                        ByteBuffer[] components = baseComparator.split(column.name());
                        ByteBuffer keyBuf = components[components.length - 1];
                        if (valueValidator instanceof MapType) {
                            actualColumnName = actualColumnName + "." + keyType.compose(keyBuf);
                            valueValidator = valueType;
                        } else if (valueValidator instanceof SetType) {
                            colValueToUse = keyBuf;
                            valueValidator = keyType;
                        } else {
                            valueValidator = valueType;
                        }
                    }
                    if (groupBy != null && groupBy.equalsIgnoreCase(actualColumnName)) {
                        group = valueValidator.getString(colValueToUse);
                    }
                    if (field.equalsIgnoreCase(actualColumnName)) {
                        colValue = colValueToUse;
                    }
                }
                ByteBuffer currentValue = (ByteBuffer) grouped.singleValue(group);
                if (currentValue == null) currentValue = colValue;
                if (reverse) {
                    grouped.singleValue(group, valueValidator.compare(currentValue, colValue) > 0 ? currentValue : colValue);
                } else
                    grouped.singleValue(group, valueValidator.compare(currentValue, colValue) < 0 ? currentValue : colValue);

            }
            if (groupBy == null)
                return singleRow(valueValidator.getString((ByteBuffer) grouped.singleValue(DEFAULT)), customColumnFactory, table, currentIndex);
            else
                return row(customColumnFactory, table, currentIndex, grouped, valueValidator, true);
        }
    }

    private List<Row> byPassRowFetch(RowScanner rowScanner, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex, Grouped grouped) {
        Options options = rowScanner.getOptions();
        Iterator<IndexEntryCollector.IndexEntry> indexIterator = rowScanner.getCollector().docs().iterator();
        String field = getField();
        String groupBy = getGroupBy();
        AbstractType valueType = getFieldValidator(options, field);
        AbstractType groupValidator = getFieldValidator(options, groupBy);
        while (indexIterator.hasNext()) {
            IndexEntryCollector.IndexEntry indexEntry = indexIterator.next();
            Object value = getValue(indexEntry, field, valueType, false);
            String group = groupBy == null ? DEFAULT : (String) getValue(indexEntry, groupBy, groupValidator, true);

            if (isNumber(valueType.asCQL3Type())) {
                Number colValue = (Number) value;
                Number currentValue = (Number) grouped.singleValue(group);
                if (currentValue == null) currentValue = colValue;
                if (reverse)
                    grouped.singleValue(group, NumberComparator.compareNumbers(currentValue, colValue) > 0 ? currentValue : colValue);
                else
                    grouped.singleValue(group, NumberComparator.compareNumbers(currentValue, colValue) < 0 ? currentValue : colValue);

            } else {
                ByteBuffer colValue = (ByteBuffer) value;
                ByteBuffer currentValue = (ByteBuffer) grouped.singleValue(group);
                if (currentValue == null) currentValue = colValue;
                if (reverse)
                    grouped.singleValue(group, valueType.compare(currentValue, colValue) > 0 ? currentValue : colValue);
                else
                    grouped.singleValue(group, valueType.compare(currentValue, colValue) < 0 ? currentValue : colValue);


            }

        }
        if (isNumber(valueType.asCQL3Type())) {
            if (groupBy == null)
                return singleRow(grouped.singleValue(DEFAULT).toString(), customColumnFactory, table, currentIndex);
            else
                return row(customColumnFactory, table, currentIndex, grouped, valueType, false);
        } else {
            if (groupBy == null)
                return singleRow(valueType.getString((ByteBuffer) grouped.singleValue(DEFAULT)), customColumnFactory, table, currentIndex);
            else
                return row(customColumnFactory, table, currentIndex, grouped, valueType, true);
        }
    }

    private List<Row> row(CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex, Grouped grouped, AbstractType valueType, boolean useValueTypeToStringify) {
        Map<String, Object> groupsAndValues = grouped.singleValued;
        String result = "{";
        boolean first = true;
        for (Map.Entry<String, Object> group : groupsAndValues.entrySet()) {
            if (!first)
                result += ",";
            result += "'" + group.getKey() + "':";
            result += useValueTypeToStringify ? valueType.getString((ByteBuffer) group.getValue()) : group.getValue();
            result += "";
            first = false;
        }
        result += "}";
        return singleRow(result, customColumnFactory, table, currentIndex);
    }
}
