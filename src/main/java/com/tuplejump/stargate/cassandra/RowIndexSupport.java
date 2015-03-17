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
import com.tuplejump.stargate.IndexContainer;
import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.LuceneUtils;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.lucene.json.JsonDocument;
import com.tuplejump.stargate.lucene.json.StreamingJsonDocument;
import com.tuplejump.stargate.utils.Pair;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.marshal.*;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 * Interface for writing a row to a lucene index.
 */
public class RowIndexSupport {
    protected static final Logger logger = LoggerFactory.getLogger(RowIndexSupport.class);
    protected Options options;
    public final ColumnFamilyStore table;
    FieldType tsFieldType;
    public final IndexContainer indexContainer;
    public final String keyspace;

    public RowIndexSupport(String keyspace, IndexContainer indexContainer, Options options, ColumnFamilyStore table) {
        this.options = options;
        this.table = table;
        this.keyspace = keyspace;
        this.indexContainer = indexContainer;
        tsFieldType = CassandraUtils.fieldType(Properties.ID_FIELD, CQL3Type.Native.BIGINT.getType());
    }

    public CFMetaData getCFMetaData() {
        return this.table.metadata;
    }

    public Options getOptions() {
        return options;
    }

    /**
     * Writes one row to the lucene index.
     *
     * @param rowKey The shard key for this row.
     * @param cf     the row to write.
     */

    public void indexRow(ByteBuffer rowKey, ColumnFamily cf) {
        Indexer indexer = indexContainer.indexer(table.partitioner.decorateKey(rowKey));
        Map<ByteBuffer, List<Field>> primaryKeysVsFields = new HashMap<>();
        DecoratedKey dk = table.partitioner.decorateKey(rowKey);
        Map<ByteBuffer, Long> timestamps = new HashMap<>();
        AbstractType rowKeyValidator = table.getComparator();
        Iterator<Column> cols = cf.iterator();
        Map<ByteBuffer, String> pkNames = new HashMap<>();
        if (cols.hasNext()) {
            while (cols.hasNext()) {
                Column column = cols.next();
                addColumn(rowKey, pkNames, primaryKeysVsFields, timestamps, column);
            }
            addToIndex(indexer, cf, dk, pkNames, primaryKeysVsFields, timestamps, rowKeyValidator);
        } else {
            DeletionInfo deletionInfo = cf.deletionInfo();
            if (cf.isMarkedForDelete()) {
                deleteRowsMarked(rowKey, indexer, deletionInfo);
            }
        }

    }

    public void deleteRowsMarked(ByteBuffer rowKey, Indexer indexer, DeletionInfo deletionInfo) {
        //this is a delete
        //get the range tombstones
        Iterator<RangeTombstone> rangeIterator = deletionInfo.rangeIterator();
        while (rangeIterator.hasNext()) {
            RangeTombstone rangeTombstone = rangeIterator.next();
            ByteBuffer start = rangeTombstone.min;
            String startPK = primaryKeyAndActualColumnName(false, rowKey, start).right;
            ByteBuffer end = rangeTombstone.max;
            String endPK = primaryKeyAndActualColumnName(false, rowKey, end).right;
            Query deleteQuery = LuceneUtils.getPKRangeDeleteQuery(startPK, endPK);
            indexer.delete(deleteQuery);
        }
    }

    private void addToIndex(Indexer indexer, ColumnFamily cf, DecoratedKey dk, Map<ByteBuffer, String> pkNames, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, Long> timestamps, AbstractType rkValValidator) {
        for (Map.Entry<ByteBuffer, List<Field>> entry : primaryKeysVsFields.entrySet()) {
            ByteBuffer pk = entry.getKey();
            String pkName = pkNames.get(pk);
            List<Field> fields = entry.getValue();
            Term term = LuceneUtils.idTerm(pkName);

            if (logger.isDebugEnabled())
                logger.debug("Column family update -" + dk);
            fields.addAll(idFields(dk, pkName, pk, rkValValidator));
            fields.addAll(tsFields(timestamps.get(pk)));
            indexer.upsert(term, fields);
        }
    }

    private void addColumn(ByteBuffer rowKey, Map<ByteBuffer, String> pkNames, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, Long> timestamps, Column column) {
        ByteBuffer columnNameBuf = column.name();
        Pair<Pair<CompositeType.Builder, StringBuilder>, String> primaryKeyAndName = primaryKeyAndActualColumnName(true, rowKey, column.name());
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
        FieldType docValueType = options.fieldDocValueTypes.get(keyColumnName);
        if (docValueType != null)
            addField(fields, columnDefinition, keyColumnName, docValueType, value);
    }

    public Pair<Pair<CompositeType.Builder, StringBuilder>, String> primaryKeyAndActualColumnName(boolean withPkBuilder, ByteBuffer rowKey, ByteBuffer columnName) {
        AbstractType<?> rowKeyComparator = table.metadata.getKeyValidator();
        CompositeType baseComparator = (CompositeType) table.getComparator();
        CFDefinition cfDef = table.metadata.getCfDef();
        int prefixSize = baseComparator.types.size() - (cfDef.hasCollections ? 2 : 1);
        List<AbstractType<?>> types = baseComparator.types;
        int idx = types.get(types.size() - 1) instanceof ColumnToCollectionType ? types.size() - 2 : types.size() - 1;
        ByteBuffer[] components = baseComparator.split(columnName);
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

    /**
     * This is used to derive the actual column name from the byte buffer column name for a single column.
     * For Wide row tables, the column names are actually a concatenation of the Clustering key column names and this column name itself.
     *
     * @param name The CQL name buffer of the column.
     * @return The String name of the column
     */

    public String getActualColumnName(ByteBuffer name) {
        ByteBuffer colName = ((CompositeType) table.getComparator()).extractLastComponent(name);
        return CassandraUtils.getColumnNameStr(colName);
    }


    protected List<Field> collectionFields(CollectionType validator, String colName, Column column) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        ByteBuffer[] components = baseComparator.split(column.name());
        List<Field> fields = new ArrayList<>();
        FieldType[] fieldTypesArr = options.collectionFieldTypes.get(colName);
        FieldType docValueType = options.collectionFieldDocValueTypes.get(colName);
        AbstractType keyType = validator.nameComparator();
        AbstractType valueType = validator.valueComparator();
        if (validator instanceof MapType) {
            ByteBuffer keyBuf = components[components.length - 1];
            if (fieldTypesArr != null) {
                fields.add(Fields.field(colName + "._key", keyType, keyBuf, fieldTypesArr[0]));
                fields.add(Fields.field(colName + "._value", valueType, column.value(), fieldTypesArr[1]));
                fields.add(Fields.field((colName + "." + keyType.getString(keyBuf)).toLowerCase(), valueType, column.value(), fieldTypesArr[1]));
            }
            if (docValueType != null)
                fields.add(Fields.field((colName + "." + keyType.getString(keyBuf)).toLowerCase(), valueType, column.value(), docValueType));
        } else if (validator instanceof SetType) {
            if (fieldTypesArr != null)
                fields.add(Fields.field(colName, keyType, components[components.length - 1], fieldTypesArr[0]));
            if (docValueType != null)
                fields.add(Fields.field(colName, keyType, components[components.length - 1], docValueType));
        } else if (validator instanceof ListType) {
            if (fieldTypesArr != null)
                fields.add(Fields.field(colName, valueType, column.value(), fieldTypesArr[0]));
            if (docValueType != null)
                fields.add(Fields.field(colName, valueType, column.value(), docValueType));
        } else throw new UnsupportedOperationException("Unsupported collection type " + validator);

        return fields;
    }

    protected List<Field> idFields(DecoratedKey rowKey, String pkName, ByteBuffer pk, AbstractType rkValValidator) {
        return Arrays.asList(LuceneUtils.rkBytesDocValue(rowKey.key), LuceneUtils.pkBytesDocValue(pk), LuceneUtils.pkNameDocValue(pkName), LuceneUtils.rowKeyIndexed(table.metadata.getKeyValidator().getString(rowKey.key)));
    }

    protected List<Field> tsFields(long ts) {
        Field tsField = LuceneUtils.tsField(ts, tsFieldType);
        return Arrays.asList(LuceneUtils.tsDocValues(ts), tsField);
    }


    protected void addField(List<Field> fields, ColumnDefinition columnDefinition, String name, FieldType fieldType, ByteBuffer value) {
        if (fieldType != null) {
            try {
                Field field = Fields.field(name, columnDefinition.getValidator(), value, fieldType);
                fields.add(field);
            } catch (Exception e) {
                logger.warn("Could not index column", e);
            }
        }
    }

    protected void addFields(Column column, String name, List<Field> fields, ColumnDefinition columnDefinition) {
        boolean isObject = options.isObject(name);
        if (isObject) {
            String value = UTF8Type.instance.compose(column.value());
            JsonDocument document = new StreamingJsonDocument(value, options.primary, name);
            fields.addAll(document.getFields());
        } else if (columnDefinition.getValidator().isCollection()) {
            List<Field> fieldsForField = collectionFields((CollectionType) columnDefinition.getValidator(), name, column);
            fields.addAll(fieldsForField);
        } else {
            FieldType fieldType = options.fieldTypes.get(name);
            addField(fields, columnDefinition, name, fieldType, column.value());
            FieldType docValueType = options.fieldDocValueTypes.get(name);
            if (docValueType != null)
                addField(fields, columnDefinition, name, docValueType, column.value());
        }
    }


}
