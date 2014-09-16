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
        ByteBuffer currentValue = null;
        String currentValueStr = null;
        for (Row row : rows) {
            Collection<Column> cols = row.cf.getSortedColumns();
            for (Column column : cols) {
                if (field.equalsIgnoreCase(Utils.getColumnNameStr(baseComparator, column.name()))) {
                    AbstractType<?> type = table.metadata.getValueValidatorFromColumnName(column.name());
                    if (currentValue == null) {
                        currentValue = column.value();
                    } else {
                        if (reverse) {
                            currentValue = type.compare(currentValue, column.value()) > 0 ? currentValue : column.value();
                        } else
                            currentValue = type.compare(currentValue, column.value()) < 0 ? currentValue : column.value();
                    }
                    currentValueStr = type.getString(currentValue);
                }
            }
        }
        return singleRow(currentValue == null ? null : currentValueStr, customColumnFactory, table, currentIndex);
    }
}
