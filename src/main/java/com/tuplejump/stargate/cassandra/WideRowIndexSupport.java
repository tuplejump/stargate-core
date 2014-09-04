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
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.ColumnToCollectionType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.Term;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 * Support for indexing rows of Wide tables(tables with clustering columns).
 */
public class WideRowIndexSupport extends RowIndexSupport {

    public WideRowIndexSupport(Options options, ColumnFamilyStore table) {
        super(options, table);
    }

    @Override
    public void indexRow(Indexer indexer, ByteBuffer rowKey, ColumnFamily cf) {
        Map<ByteBuffer, List<Field>> primaryKeysVsFields = new HashMap<>();
        DecoratedKey dk = table.partitioner.decorateKey(rowKey);
        Map<ByteBuffer, Long> timestamps = new HashMap<>();
        AbstractType rowKeyValidator = table.getComparator();
        Iterator<Column> cols = cf.iterator();
        Map<ByteBuffer, String> pkNames = new HashMap<>();
        while (cols.hasNext()) {
            Column column = cols.next();
            addColumn(rowKey, pkNames, primaryKeysVsFields, timestamps, column);
        }
        addToIndex(indexer, cf, dk, pkNames, primaryKeysVsFields, timestamps, rowKeyValidator);
    }

    private void addToIndex(Indexer indexer, ColumnFamily cf, DecoratedKey dk, Map<ByteBuffer, String> pkNames, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, Long> timestamps, AbstractType rkValValidator) {
        for (Map.Entry<ByteBuffer, List<Field>> entry : primaryKeysVsFields.entrySet()) {
            ByteBuffer pk = entry.getKey();
            String pkName = pkNames.get(pk);
            List<Field> fields = entry.getValue();
            Term term = Fields.idTerm(pkName);

            if (cf.isMarkedForDelete() && options.collectionFieldTypes.isEmpty()) {
                if (logger.isDebugEnabled())
                    logger.debug("Column family marked for delete -" + dk);
                if (logger.isDebugEnabled())
                    logger.debug(String.format("RowIndex delete - Key [%s]", term));
                indexer.delete(term);
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("Column family update -" + dk);
                fields.addAll(idFields(dk, pkName, pk, rkValValidator));
                fields.addAll(tsFields(timestamps.get(pk)));
                indexer.insert(fields);
            }
        }
    }

    private void addColumn(ByteBuffer rowKey, Map<ByteBuffer, String> pkNames, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, Long> timestamps, Column column) {
        ByteBuffer columnNameBuf = column.name();
        Pair<Pair<CompositeType.Builder, StringBuilder>, String> primaryKeyAndName = primaryKeyAndActualColumnName(true, rowKey, column);
        String actualColName = primaryKeyAndName.right;
        if (logger.isTraceEnabled())
            logger.trace("Got column name {} from CF", actualColName);
        Pair<CompositeType.Builder, StringBuilder> builders = primaryKeyAndName.left;
        ByteBuffer primaryKey = builders.left.build();
        pkNames.put(primaryKey, builders.right.toString());
        List<Field> fields = primaryKeysVsFields.get(primaryKey);
        if (fields == null) {
            // new pk found
            if (logger.isTraceEnabled()) {
                logger.trace("New PK found");
            }
            fields = new LinkedList<>();
            primaryKeysVsFields.put(primaryKey, fields);
            timestamps.put(primaryKey, 0l);
            CFDefinition cfDef = table.metadata.getCfDef();

            //fields for partition key columns need to be added.
            addPartitionKeyFields(rowKey, timestamps, column, primaryKey, fields, cfDef);

            //fields for clustering key columns need to be added.
            addClusteringKeyFields(options.clusteringKeysIndexed, primaryKey, fields, timestamps, column, builders.left);
        }
        ColumnDefinition columnDefinition = table.metadata.getColumnDefinitionFromColumnName(columnNameBuf);
        if (options.shouldIndex(actualColName)) {
            long existingTS = timestamps.get(primaryKey);
            timestamps.put(primaryKey, Math.max(existingTS, column.maxTimestamp()));
            addFields(column, actualColName, fields, columnDefinition);
        }
    }

    private void addPartitionKeyFields(ByteBuffer rowKey, Map<ByteBuffer, Long> timestamps, Column column, ByteBuffer primaryKey, List<Field> fields, CFDefinition cfDef) {
        ByteBuffer[] keyComponents = cfDef.hasCompositeKey ? ((CompositeType) table.metadata.getKeyValidator()).split(rowKey) : new ByteBuffer[]{rowKey};
        for (Map.Entry<Integer, Pair<String, ByteBuffer>> entry : options.partitionKeysIndexed.entrySet()) {
            ByteBuffer value = keyComponents[entry.getKey()];
            addKeyField(primaryKey, fields, timestamps, column, entry, value);
        }
    }

    private void addClusteringKeyFields(Map<Integer, Pair<String, ByteBuffer>> keyFields, ByteBuffer primaryKey, List<Field> fields, Map<ByteBuffer, Long> timestamps, Column column, CompositeType.Builder builder) {
        for (Map.Entry<Integer, Pair<String, ByteBuffer>> entry : keyFields.entrySet()) {
            ByteBuffer value = builder.get(entry.getKey());
            addKeyField(primaryKey, fields, timestamps, column, entry, value);
        }
    }

    private void addKeyField(ByteBuffer primaryKey, List<Field> fields, Map<ByteBuffer, Long> timestamps, Column column, Map.Entry<Integer, Pair<String, ByteBuffer>> entry, ByteBuffer value) {
        ByteBuffer keyColumn = entry.getValue().right;
        ColumnDefinition columnDefinition = table.metadata.getColumnDefinition(keyColumn);
        String keyColumnName = entry.getValue().left;
        FieldType fieldType = options.fieldTypes.get(keyColumnName);
        long existingTS = timestamps.get(primaryKey);
        timestamps.put(primaryKey, Math.max(existingTS, column.maxTimestamp()));
        addField(fields, columnDefinition, keyColumnName, fieldType, value);
    }

    public Pair<Pair<CompositeType.Builder, StringBuilder>, String> primaryKeyAndActualColumnName(boolean withPkBuilder, ByteBuffer rowKey, Column column) {
        AbstractType<?> rowKeyComparator = table.metadata.getKeyValidator();
        CompositeType baseComparator = (CompositeType) table.getComparator();
        CFDefinition cfDef = table.metadata.getCfDef();
        int prefixSize = baseComparator.types.size() - (cfDef.hasCollections ? 2 : 1);
        List<AbstractType<?>> types = baseComparator.types;
        int idx = types.get(types.size() - 1) instanceof ColumnToCollectionType ? types.size() - 2 : types.size() - 1;
        ByteBuffer[] components = baseComparator.split(column.name());
        String colName = CFDefinition.definitionType.getString(components[idx]);
        if (withPkBuilder) {
            Pair<CompositeType.Builder, StringBuilder> builder = pkBuilder(rowKeyComparator, rowKey, baseComparator, prefixSize, components);
            return Pair.create(builder, colName);
        } else {
            return Pair.create(null, colName);
        }
    }

    private Pair<CompositeType.Builder, StringBuilder> pkBuilder(AbstractType rowKeyComparator, ByteBuffer rowKey, CompositeType baseComparator, int prefixSize, ByteBuffer[] components) {
        List<AbstractType<?>> types = baseComparator.types;
        StringBuilder sb = new StringBuilder();
        CompositeType.Builder builder = new CompositeType.Builder(baseComparator);
        builder.add(rowKey);
        sb.append(rowKeyComparator.getString(rowKey));
        for (int i = 0; i < Math.min(prefixSize, components.length); i++) {
            builder.add(components[i]);
            AbstractType<?> componentType = types.get(i);
            sb.append(':').append(componentType.compose(components[i]));
        }
        return Pair.create(builder, sb);
    }


    public String getActualColumnName(ByteBuffer name) {
        ByteBuffer colName = ((CompositeType) table.getComparator()).extractLastComponent(name);
        return Utils.getColumnNameStr(colName);
    }

}
