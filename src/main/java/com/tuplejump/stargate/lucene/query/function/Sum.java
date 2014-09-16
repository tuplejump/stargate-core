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
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: satya
 */
public class Sum extends Aggregate {

    @JsonCreator
    public Sum(@JsonProperty("field") String field, @JsonProperty("name") String name, @JsonProperty("distinct") boolean distinct, @JsonProperty("groupBy") String groupBy) {
        super(field, name, distinct, groupBy);
    }

    public String getFunction() {
        return "sum";
    }

    @Override
    public List<Row> process(List<Row> rows, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        boolean numberCheck = false;
        Grouped grouped = new Grouped(false);
        for (Row row : rows) {
            String group = DEFAULT;
            double sum = 0;
            ColumnFamily cf = row.cf;
            Collection<Column> cols = cf.getSortedColumns();
            for (Column column : cols) {
                String actualColumnName = Utils.getColumnNameStr(baseComparator, column.name());
                AbstractType<?> valueValidator = table.metadata.getValueValidatorFromColumnName(column.name());
                if (groupBy != null && groupBy.equalsIgnoreCase(actualColumnName)) {
                    group = valueValidator.getString(column.value());
                }
                if (field.equalsIgnoreCase(actualColumnName)) {
                    CQL3Type cqlType = valueValidator.asCQL3Type();
                    if (!numberCheck && !isNumber(cqlType)) {
                        throw new UnsupportedOperationException("Sum function is available only on numeric types");
                    }
                    numberCheck = true;
                    Object obj = valueValidator.compose(column.value());
                    if (cqlType == CQL3Type.Native.INT || cqlType == CQL3Type.Native.VARINT) {
                        sum += (Integer) obj;
                    } else if (cqlType == CQL3Type.Native.BIGINT) {
                        sum += (Long) obj;
                    } else if (cqlType == CQL3Type.Native.FLOAT) {
                        sum += (Float) obj;
                    } else if (cqlType == CQL3Type.Native.DECIMAL) {
                        sum += ((BigDecimal) obj).doubleValue();
                    } else if (cqlType == CQL3Type.Native.DOUBLE) {
                        sum += (Double) obj;
                    }
                }
            }
            Double singleValue = (Double) grouped.singleValue(group);
            if (singleValue == null) grouped.singleValue(group, sum);
            else grouped.singleValue(group, sum + singleValue);
        }
        if (groupBy == null)
            return singleRow(grouped.singleValue(DEFAULT).toString(), customColumnFactory, table, currentIndex);
        else
            return row(customColumnFactory, table, currentIndex, grouped);
    }

    private List<Row> row(CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex, Grouped grouped) {
        Map<String, Object> groupsAndValues = grouped.singleValued;
        String result = "{";
        boolean first = true;
        for (Map.Entry<String, Object> group : groupsAndValues.entrySet()) {
            if (!first)
                result += ",";
            result += "'" + group.getKey() + "':";
            result += group.getValue();
            result += "";
            first = false;
        }
        result += "}";
        return singleRow(result, customColumnFactory, table, currentIndex);
    }


}
