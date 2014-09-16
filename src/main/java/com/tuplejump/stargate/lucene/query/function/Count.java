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
import com.tuplejump.stargate.cassandra.CustomColumnFactory;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
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
    public List<Row> process(List<Row> rows, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        Grouped grouped = values(rows, table);
        if (groupBy == null) {
            int count;
            if (distinct && field != null) {
                count = grouped.values(DEFAULT).size();
            } else {
                count = rows.size();
            }
            return singleRow("" + count, customColumnFactory, table, currentIndex);
        } else {
            return row(customColumnFactory, table, currentIndex, grouped);
        }
    }

    private List<Row> row(CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex, Grouped grouped) {
        Map<String, Collection<Object>> groupsAndValues = grouped.multiValued;
        String result = "{";
        boolean first = true;
        for (Map.Entry<String, Collection<Object>> group : groupsAndValues.entrySet()) {
            if (!first)
                result += ",";
            result += "'" + group.getKey() + "':";
            result += group.getValue().size();
            result += "";
            first = false;
        }
        result += "}";
        return singleRow(result, customColumnFactory, table, currentIndex);
    }
}
