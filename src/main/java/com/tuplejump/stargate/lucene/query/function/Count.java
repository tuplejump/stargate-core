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
import com.tuplejump.stargate.cassandra.RowScanner;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: satya
 */
public class Count extends Aggregate {

    @JsonCreator
    public Count(@JsonProperty("field") String field, @JsonProperty("name") String name, @JsonProperty("distinct") boolean distinct, @JsonProperty("groupBy") String groupBy) {
        super(field, name, distinct, groupBy);
    }

    public String getFunction() {
        return "count";
    }

    @Override
    public List<Row> process(RowScanner rowScanner, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        if (groupBy == null && !distinct)
            return singleRow("" + rowScanner.getCollector().docs().size(), customColumnFactory, table, currentIndex);

        if (groupBy != null && distinct) {
            Grouped grouped = values(rowScanner, table);
            return distinctSize(customColumnFactory, table, currentIndex, grouped);
        }

        CompositeType baseComparator = (CompositeType) table.getComparator();
        Grouped grouped = new Grouped(false);
        while (rowScanner.hasNext()) {
            Row row = rowScanner.next();
            String group = DEFAULT;
            long count = 0;
            ColumnFamily cf = row.cf;
            Collection<Column> cols = cf.getSortedColumns();
            for (Column column : cols) {
                String actualColumnName = Utils.getColumnNameStr(baseComparator, column.name());
                AbstractType<?> valueValidator = table.metadata.getValueValidatorFromColumnName(column.name());
                if (groupBy != null && groupBy.equalsIgnoreCase(actualColumnName)) {
                    group = valueValidator.getString(column.value());
                }
                if (actualColumnName.equalsIgnoreCase(field)) {
                    count += 1;
                }
            }
            Long singleValue = (Long) grouped.singleValue(group);
            if (singleValue == null) grouped.singleValue(group, count);
            else grouped.singleValue(group, count + singleValue);
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
            result += "'" + group.getKey() + "':" + group.getValue();
            first = false;
        }
        result += "}";
        return singleRow(result, customColumnFactory, table, currentIndex);
    }

    private List<Row> distinctSize(CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex, Grouped grouped) {
        Map<String, Collection<Object>> groupsAndValues = grouped.multiValued;
        String result = "{";
        boolean first = true;
        for (Map.Entry<String, Collection<Object>> group : groupsAndValues.entrySet()) {
            if (!first)
                result += ",";
            result += "'" + group.getKey() + "':" + group.getValue().size();
            first = false;
        }
        result += "}";
        return singleRow(result, customColumnFactory, table, currentIndex);
    }
}

