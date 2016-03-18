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
import com.tuplejump.stargate.lucene.*;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.lucene.json.JsonDocument;
import com.tuplejump.stargate.lucene.json.StreamingJsonDocument;
import com.tuplejump.stargate.utils.Pair;
import javolution.util.FastList;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.composites.CType;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.filter.ColumnSlice;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.serializers.MarshalException;
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
    FieldType tsFieldType;
    public final IndexContainer indexContainer;
    public final String keyspace;
    public final TableMapper tableMapper;


    public RowIndexSupport(String keyspace, IndexContainer indexContainer, Options options, TableMapper tableMapper) {
        this.options = options;
        this.tableMapper = tableMapper;
        this.keyspace = keyspace;
        this.indexContainer = indexContainer;
        tsFieldType = CassandraUtils.fieldType(Properties.ID_FIELD, CQL3Type.Native.BIGINT.getType());
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
        DecoratedKey dk = tableMapper.decorateKey(rowKey);
        Indexer indexer = indexContainer.indexer(dk);
        IndexEntryBuilder builder = new IndexEntryBuilder();
        Iterator<Cell> cols = cf.iterator();
        if (cols.hasNext()) {
            while (cols.hasNext()) {
                addCell(rowKey, builder, cols.next());
            }
            builder.finishLast();
            addToIndex(indexer, dk, builder);
        } else {
            DeletionInfo deletionInfo = cf.deletionInfo();
            if (deletionInfo != null && cf.isMarkedForDelete()) {
                if (deletionInfo.rangeIterator().hasNext()) {
                    deleteRowsMarked(indexer, deletionInfo);
                } else {
                    //remove the partition
                    String rkString = rowKeyString(dk);
                    Term rowkeyTerm = LuceneUtils.rowkeyTerm(rkString);
                    indexer.delete(rowkeyTerm);
                }
            }
        }

    }

    public void deleteRowsMarked(Indexer indexer, DeletionInfo deletionInfo) {
        //this is a delete
        //get the range tombstones
        Iterator<RangeTombstone> rangeIterator = deletionInfo.rangeIterator();
        while (rangeIterator.hasNext()) {
            RangeTombstone rangeTombstone = rangeIterator.next();
            Composite start = rangeTombstone.min;
            String startPK = tableMapper.clusteringCType.getString(start);
            Composite end = rangeTombstone.max;
            String endPK = tableMapper.clusteringCType.getString(end);
            Query deleteQuery = LuceneUtils.getPKRangeDeleteQuery(startPK, endPK);
            indexer.delete(deleteQuery);
        }
    }

    private void addToIndex(Indexer indexer, DecoratedKey dk, IndexEntryBuilder builder) {
        List<Pair<String, ByteBuffer>> primaryKeys = builder.primaryKeys;
        List<Long> timestamps = builder.timestamps;
        List<List<Field>> entries = builder.entries;
        for (int i = 0; i < primaryKeys.size(); i++) {
            Pair pkPair = primaryKeys.get(i);
            String pkName = (String) pkPair.left;
            ByteBuffer pkBuf = (ByteBuffer) pkPair.right;
            List<Field> fields = entries.get(i);
            boolean isPartialUpdate = false;
            if (fields.size() < options.fieldTypes.size()) {
                if (logger.isDebugEnabled())
                    logger.debug("Column family update -" + dk);
                isPartialUpdate = true;
            }
            fields.addAll(idFields(dk, pkName, pkBuf));
            long ts = timestamps.get(i);
            fields.add(LuceneUtils.tsField(ts, tsFieldType));
            if (isPartialUpdate) {
                loadOldRow(dk, pkBuf, fields);
            }
            Term pkTerm = new Term(LuceneUtils.PK_INDEXED, LuceneUtils.primaryKeyField(pkName).stringValue());
            indexer.upsert(pkTerm, fields);
        }
    }

    private void loadOldRow(DecoratedKey dk, ByteBuffer pkBuf, List<Field> fields) {
        CellName clusteringKey = tableMapper.makeClusteringKey(pkBuf);
        Composite start = tableMapper.start(clusteringKey);
        Composite end = tableMapper.end(start);
        ColumnSlice columnSlice = new ColumnSlice(start, end);
        SliceQueryFilter sliceQueryFilter = new SliceQueryFilter(columnSlice, false, Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter(dk, tableMapper.table.name, sliceQueryFilter, new Date().getTime());
        ColumnFamily columnFamily = tableMapper.table.getColumnFamily(queryFilter);
        Map<CellName, ColumnFamily> fullSlice = tableMapper.getRows(columnFamily);
        ColumnFamily oldDocument = fullSlice.get(clusteringKey);

        for (Cell cell : oldDocument) {
            CellName cellName = cell.name();
            ColumnIdentifier cql3ColName = cellName.cql3ColumnName(tableMapper.cfMetaData);
            String actualColName = cql3ColName.toString();
            ColumnDefinition columnDefinition = tableMapper.cfMetaData.getColumnDefinition(cql3ColName);
            if (options.shouldIndex(actualColName)) {
                addFields(cell, actualColName, columnDefinition, fields);
            }
        }
    }

    private void addCell(ByteBuffer rowKey, IndexEntryBuilder builder, Cell cell) {
        CellName cellName = cell.name();
        ColumnIdentifier cql3ColName = cellName.cql3ColumnName(tableMapper.cfMetaData);
        String actualColName = cql3ColName.toString();
        if (logger.isTraceEnabled())
            logger.trace("Got column name {} from CF", actualColName);

        CellName clusteringKey = tableMapper.extractClusteringKey(cell.name());
        ByteBuffer primaryKeyBuff = tableMapper.primaryKey(rowKey, clusteringKey);
        String primaryKey = tableMapper.primaryKeyType.getString(primaryKeyBuff);
        if (builder.isNew(primaryKey)) {
            builder.newPrimaryKey(primaryKey, primaryKeyBuff);
            // new pk found
            if (logger.isTraceEnabled()) {
                logger.trace("New PK found {}", primaryKey);
            }
            //fields for partition key columns need to be added.
            addPartitionKeyFields(rowKey, cell.timestamp(), builder);

            //fields for clustering key columns need to be added.
            addClusteringKeyFields(clusteringKey, cell.timestamp(), builder);
        }
        addCell(cell, cql3ColName, actualColName, builder);
    }

    private void addCell(Cell cell, ColumnIdentifier cql3ColName, String actualColName, IndexEntryBuilder builder) {
        ColumnDefinition columnDefinition = tableMapper.cfMetaData.getColumnDefinition(cql3ColName);
        if (options.shouldIndex(actualColName)) {
            builder.setCurrentTimestamp(cell.timestamp());
            List<Field> fields = builder.getFieldList();
            addFields(cell, actualColName, columnDefinition, fields);
        }
    }

    private void addPartitionKeyFields(ByteBuffer rowKey, long timestamp, IndexEntryBuilder builder) {
        CType keyCType = tableMapper.cfMetaData.getKeyValidatorAsCType();
        Composite compoundRowKey = keyCType.fromByteBuffer(rowKey);
        for (Map.Entry<String, ColumnDefinition> entry : options.partitionKeysIndexed.entrySet()) {
            ByteBuffer value = compoundRowKey.get(entry.getValue().position());
            addKeyField(timestamp, entry, value, builder);
        }
    }

    private void addClusteringKeyFields(CellName clusteringKey, long timestamp, IndexEntryBuilder builder) {
        for (Map.Entry<String, ColumnDefinition> entry : options.clusteringKeysIndexed.entrySet()) {
            ByteBuffer value = clusteringKey.get(entry.getValue().position());
            addKeyField(timestamp, entry, value, builder);
        }
    }

    private void addKeyField(long timestamp, Map.Entry<String, ColumnDefinition> entry, ByteBuffer value, IndexEntryBuilder builder) {
        String keyColumnName = entry.getValue().name.toString();
        builder.setCurrentTimestamp(timestamp);
        List<Field> fields = builder.getFieldList();
        FieldType fieldType = options.fieldTypes.get(keyColumnName);
        Type type = options.types.get(keyColumnName);

        addField(type, entry.getValue(), keyColumnName, fieldType, value, fields);
        if (options.containsDocValues()) {
            FieldType docValueType = options.fieldDocValueTypes.get(keyColumnName);
            if (docValueType != null) {
                Field docValueField = Fields.docValueField(keyColumnName, entry.getValue().type, value, docValueType);
                fields.add(docValueField);

            }
        }
    }


    protected List<Field> collectionFields(CollectionType validator, String colName, Cell column) {
        CellName cellName = column.name();
        List<Field> fields = new ArrayList<>();
        FieldType[] fieldTypesArr = options.collectionFieldTypes.get(colName);
        FieldType docValueType = options.collectionFieldDocValueTypes.get(colName);
        AbstractType keyType = validator.nameComparator();
        FieldCreator keyFieldCreator = CassandraUtils.fromAbstractType(keyType.asCQL3Type()).fieldCreator;
        AbstractType valueType = validator.valueComparator();
        FieldCreator valueFieldCreator = CassandraUtils.fromAbstractType(valueType.asCQL3Type()).fieldCreator;
        ByteBuffer collectionElement = cellName.collectionElement();
        if (validator instanceof MapType) {
            if (fieldTypesArr != null) {
                fields.add(keyFieldCreator.field(colName + "._key", keyType, collectionElement, fieldTypesArr[0]));
                fields.add(valueFieldCreator.field(colName + "._value", valueType, column.value(), fieldTypesArr[1]));
                fields.add(valueFieldCreator.field((colName + "." + keyType.getString(collectionElement)).toLowerCase(), valueType, column.value(), fieldTypesArr[1]));
            }
            if (docValueType != null)
                fields.add(Fields.docValueField((colName + "." + keyType.getString(collectionElement)).toLowerCase(), valueType, column.value(), docValueType));
        } else if (validator instanceof SetType) {
            if (fieldTypesArr != null)
                fields.add(keyFieldCreator.field(colName, keyType, collectionElement, fieldTypesArr[0]));
            if (docValueType != null)
                fields.add(Fields.docValueField(colName, keyType, collectionElement, docValueType));
        } else if (validator instanceof ListType) {
            if (fieldTypesArr != null)
                fields.add(valueFieldCreator.field(colName, valueType, column.value(), fieldTypesArr[0]));
            if (docValueType != null)
                fields.add(Fields.docValueField(colName, valueType, column.value(), docValueType));
        } else throw new UnsupportedOperationException("Unsupported collection type " + validator);

        return fields;
    }

    protected List<Field> idFields(DecoratedKey rowKey, String pkName, ByteBuffer pkBuf) {
        return Arrays.asList(
                LuceneUtils.rkBytesDocValue(rowKey.getKey()),
                LuceneUtils.rowKeyIndexed(rowKeyString(rowKey)),
                LuceneUtils.pkBytesDocValue(pkBuf),
                LuceneUtils.primaryKeyField(pkName),
                LuceneUtils.tokenIndexed(rowKey.getToken()),
                LuceneUtils.tokenBytesDocValue(rowKey.getToken())
        );
    }

    private String rowKeyString(DecoratedKey rowKey) {
        return tableMapper.primaryKeyAbstractType.getString(rowKey.getKey());
    }


    protected void addField(Type type, ColumnDefinition columnDefinition, String name,
                            FieldType fieldType, ByteBuffer value, List<Field> fields) {
        if (fieldType != null) {
            try {
                Field field = type.fieldCreator.field(name, columnDefinition.type, value, fieldType);
                fields.add(field);
            } catch (MarshalException e) {
                fields.add(new Field(name, "_null_", Fields.STRING_FIELD_TYPE));
            } catch (Exception e) {
                logger.warn("Could not index column {}{}", columnDefinition, name);
                logger.warn("Exception while indexing column", e);
            }
        }
    }

    protected void addFields(Cell column, String name, ColumnDefinition columnDefinition, List<Field> fields) {
        boolean isObject = options.isObject(name);
        if (isObject) {
            String value = UTF8Type.instance.compose(column.value());
            JsonDocument document = new StreamingJsonDocument(value, options.primary, name);
            fields.addAll(document.getFields());
        } else if (column.name().isCollectionCell()) {
            List<Field> fieldsForField = collectionFields((CollectionType) columnDefinition.type, name, column);
            fields.addAll(fieldsForField);
        } else {
            FieldType fieldType = options.fieldTypes.get(name);
            Type type = options.types.get(name);
            addField(type, columnDefinition, name, fieldType, column.value(), fields);
            if (options.containsDocValues()) {
                FieldType docValueType = options.fieldDocValueTypes.get(name);
                if (docValueType != null) {
                    Field docValueField = Fields.docValueField(name, columnDefinition.type, column.value(), docValueType);
                    fields.add(docValueField);
                }
            }
        }
    }


    private static class IndexEntryBuilder {

        private List<Field> fieldList;
        private String currentKey;
        private long currentTimestamp;

        public final List<List<Field>> entries = new FastList<>();
        public final List<Pair<String, ByteBuffer>> primaryKeys = new FastList<>();
        public final List<Long> timestamps = new FastList<>();


        public boolean isNew(String primaryKey) {
            return !primaryKey.equals(currentKey);
        }

        public void newPrimaryKey(String primaryKey, ByteBuffer primaryKeyBuffer) {
            finishLast();
            currentKey = primaryKey;
            primaryKeys.add(Pair.create(primaryKey, primaryKeyBuffer));
            currentTimestamp = 0;
            fieldList = new LinkedList<>();
        }

        public void finishLast() {
            if (fieldList != null) {
                entries.add(fieldList);
                timestamps.add(currentTimestamp);
            }
        }

        public void setCurrentTimestamp(long ts) {
            currentTimestamp = Math.max(currentTimestamp, ts);
        }

        public List<Field> getFieldList() {
            return fieldList;
        }
    }

}
