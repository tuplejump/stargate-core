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
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.nio.ByteBuffer;
import java.util.Collection;
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
    public List<Row> process(List<Row> rows, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        Grouped grouped = new Grouped(false);
        AbstractType valueType = null;
        for (Row row : rows) {
            String group = DEFAULT;
            ByteBuffer colValue = null;
            Collection<Column> cols = row.cf.getSortedColumns();
            for (Column column : cols) {
                AbstractType<?> valueValidator = table.metadata.getValueValidatorFromColumnName(column.name());
                String actualColumnName = Utils.getColumnNameStr(baseComparator, column.name());
                if (groupBy != null && groupBy.equalsIgnoreCase(actualColumnName)) {
                    group = valueValidator.getString(column.value());
                }
                if (field.equalsIgnoreCase(actualColumnName)) {
                    colValue = column.value();
                    valueType = valueValidator;
                }
            }
            ByteBuffer currentValue = (ByteBuffer) grouped.singleValue(group);
            if (currentValue == null) currentValue = colValue;
            if (reverse) {
                grouped.singleValue(group, valueType.compare(currentValue, colValue) > 0 ? currentValue : colValue);
            } else
                grouped.singleValue(group, valueType.compare(currentValue, colValue) < 0 ? currentValue : colValue);

        }
        if (groupBy == null)
            return singleRow(valueType.getString((ByteBuffer) grouped.singleValue(DEFAULT)), customColumnFactory, table, currentIndex);
        else
            return row(customColumnFactory, table, currentIndex, grouped, valueType);
    }

    private List<Row> row(CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex, Grouped grouped, AbstractType valueType) {
        Map<String, Object> groupsAndValues = grouped.singleValued;
        String result = "{";
        boolean first = true;
        for (Map.Entry<String, Object> group : groupsAndValues.entrySet()) {
            if (!first)
                result += ",";
            result += "'" + group.getKey() + "':";
            result += valueType.getString((ByteBuffer) group.getValue());
            result += "";
            first = false;
        }
        result += "}";
        return singleRow(result, customColumnFactory, table, currentIndex);
    }
}
