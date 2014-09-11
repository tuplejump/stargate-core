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

package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.RowIndex;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.commons.lang3.StringEscapeUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This class makes an error row for reporting exceptions while processing search
 * User: satya
 */
public class ErrorReporter {

    public Row getErrorRow(ColumnFamilyStore table, RowIndex currentIndex, Exception e) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        int prefixSize = baseComparator.types.size() - (table.metadata.getCfDef().hasCollections ? 2 : 1);
        ColumnFamily cleanColumnFamily = TreeMapBackedSortedColumns.factory.create(table.metadata);
        ByteBuffer partitionKey;
        AbstractType keyType = table.metadata.getKeyValidator();
        if (keyType instanceof CompositeType) {
            CompositeType compositeType = ((CompositeType) keyType);
            CompositeType.Builder builder = compositeType.builder();
            compositeType.getComponents();
            for (AbstractType component : compositeType.getComponents()) {
                builder.add(Fields.defaultValue(component));
            }
            partitionKey = builder.build();
        } else {
            partitionKey = Fields.defaultValue(keyType);
        }


        List<ColumnDefinition> pkCols = table.metadata.partitionKeyColumns();
        for (ColumnDefinition columnDef : pkCols) {
            addDefaultColumn(table, columnDef, cleanColumnFamily, prefixSize, baseComparator);
        }
        List<ColumnDefinition> ckCols = table.metadata.clusteringKeyColumns();
        for (ColumnDefinition columnDef : ckCols) {
            addDefaultColumn(table, columnDef, cleanColumnFamily, prefixSize, baseComparator);
        }

        if (currentIndex.isMetaColumn()) {
            String indexColumnName = currentIndex.getPrimaryColumnName();
            Iterable<ColumnDefinition> cols = table.metadata.regularAndStaticColumns();
            for (ColumnDefinition columnDef : cols) {
                addDefaultColumn(table, columnDef, cleanColumnFamily, prefixSize, baseComparator);
            }
            addErrorColumn(table, indexColumnName, e.getMessage(), cleanColumnFamily);
            DecoratedKey dk = table.partitioner.decorateKey(partitionKey);
            return new Row(dk, cleanColumnFamily);
        } else {
            return null;
        }
    }

    public void addDefaultColumn(ColumnFamilyStore table, ColumnDefinition col, ColumnFamily cleanColumnFamily, int prefixSize, CompositeType baseComparator) {
        CompositeType.Builder builder = baseComparator.builder();
        for (int i = 0; i < prefixSize; i++)
            builder.add(Fields.defaultValue(baseComparator.types.get(i)));
        builder.add(col.name);
        ByteBuffer finalColumnName = builder.build();
        Column defaultColumn = new Column(finalColumnName, Fields.defaultValue(col.getValidator()));
        cleanColumnFamily.addColumn(defaultColumn);
    }

    public void addErrorColumn(ColumnFamilyStore table, String colName, String errorMsg, ColumnFamily cleanColumnFamily) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        int prefixSize = baseComparator.types.size() - (table.metadata.getCfDef().hasCollections ? 2 : 1);
        CompositeType.Builder builder = baseComparator.builder();
        for (int i = 0; i < prefixSize; i++)
            builder.add(Fields.defaultValue(baseComparator.types.get(i)));
        builder.add(UTF8Type.instance.decompose(colName));
        ByteBuffer finalColumnName = builder.build();
        Column scoreColumn = new Column(finalColumnName, UTF8Type.instance.decompose("{\"error\":\"" + StringEscapeUtils.escapeEcmaScript(errorMsg) + "\"}"));
        cleanColumnFamily.addColumn(scoreColumn);
    }
}
