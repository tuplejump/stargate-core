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
import javolution.util.FastMap;
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
        Map<String, List<Field>> primaryKeysVsFields = new FastMap<>();
        Map<String, Long> timestamps = new FastMap<>();
        Iterator<Cell> cols = cf.iterator();
        Map<String, ByteBuffer> pkNames = new FastMap<>();
        if (cols.hasNext()) {
            while (cols.hasNext()) {
                addCell(rowKey, pkNames, primaryKeysVsFields, timestamps, cols.next());
            }
            addToIndex(indexer, dk, pkNames, primaryKeysVsFields, timestamps);
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

    private void addToIndex(Indexer indexer, DecoratedKey dk, Map<String, ByteBuffer> pkNames, Map<String, List<Field>> primaryKeysVsFields, Map<String, Long> timestamps) {
        for (Map.Entry<String, List<Field>> entry : primaryKeysVsFields.entrySet()) {
            String pk = entry.getKey();
            ByteBuffer pkBuf = pkNames.get(pk);
            List<Field> fields = entry.getValue();
            boolean isPartialUpdate = false;
            if (fields.size() < options.fieldTypes.size()) {
                if (logger.isDebugEnabled())
                    logger.debug("Column family update -" + dk);
                isPartialUpdate = true;
            }
            fields.addAll(idFields(dk, pk, pkBuf));
            fields.addAll(tsFields(timestamps.get(pk)));
            if (isPartialUpdate) {
                CellName clusteringKey = tableMapper.makeClusteringKey(pkBuf);
                Composite start = tableMapper.start(clusteringKey);
                Composite end = tableMapper.end(start);
                ColumnSlice columnSlice = new ColumnSlice(start, end);
                SliceQueryFilter sliceQueryFilter = new SliceQueryFilter(columnSlice, false, Integer.MAX_VALUE);
                QueryFilter queryFilter = new QueryFilter(dk, tableMapper.table.name, sliceQueryFilter, new Date().getTime());
                ColumnFamily columnFamily = tableMapper.table.getColumnFamily(queryFilter);
                Map<CellName, ColumnFamily> fullSlice = tableMapper.getRows(columnFamily);
                ColumnFamily oldDocument = fullSlice.get(clusteringKey);
                //fields for partition key columns need to be added.
                addPartitionKeyFields(dk.getKey(), timestamps, oldDocument.maxTimestamp(), pk, fields);
                //fields for clustering key columns need to be added.
                addClusteringKeyFields(clusteringKey, timestamps, oldDocument.maxTimestamp(), pk, fields);

                for (Cell cell : oldDocument) {
                    CellName cellName = cell.name();
                    ColumnIdentifier cql3ColName = cellName.cql3ColumnName(tableMapper.cfMetaData);
                    String actualColName = cql3ColName.toString();
                    addCell(timestamps, cell, cql3ColName, actualColName, pk, fields);
                }
            }
            Term pkTerm = new Term(LuceneUtils.PK_INDEXED, LuceneUtils.primaryKeyField(pk).stringValue());
            indexer.upsert(pkTerm,fields);
            //indexer.delete(pkTerm);
            //indexer.insert(fields);

        }
    }

    private void addCell(ByteBuffer rowKey, Map<String, ByteBuffer> pkNames, Map<String, List<Field>> primaryKeysVsFields, Map<String, Long> timestamps, Cell cell) {
        CellName cellName = cell.name();
        ColumnIdentifier cql3ColName = cellName.cql3ColumnName(tableMapper.cfMetaData);
        String actualColName = cql3ColName.toString();
        if (logger.isTraceEnabled())
            logger.trace("Got column name {} from CF", actualColName);

        CellName clusteringKey = tableMapper.extractClusteringKey(cell.name());
        ByteBuffer primaryKeyBuff = tableMapper.primaryKey(rowKey, clusteringKey);
        String primaryKey = tableMapper.primaryKeyType.getString(primaryKeyBuff);
        pkNames.put(primaryKey, primaryKeyBuff);

        List<Field> fields = primaryKeysVsFields.get(primaryKey);
        if (fields == null) {
            // new pk found
            if (logger.isTraceEnabled()) {
                logger.trace("New PK found {}", primaryKey);
            }
            fields = new LinkedList<>();
            primaryKeysVsFields.put(primaryKey, fields);
            timestamps.put(primaryKey, 0l);

            //fields for partition key columns need to be added.
            addPartitionKeyFields(rowKey, timestamps, cell.timestamp(), primaryKey, fields);

            //fields for clustering key columns need to be added.
            addClusteringKeyFields(clusteringKey, timestamps, cell.timestamp(), primaryKey, fields);
        }
        addCell(timestamps, cell, cql3ColName, actualColName, primaryKey, fields);
    }

    private void addCell(Map<String, Long> timestamps, Cell cell, ColumnIdentifier cql3ColName, String actualColName, String primaryKey, List<Field> fields) {
        ColumnDefinition columnDefinition = tableMapper.cfMetaData.getColumnDefinition(cql3ColName);
        if (options.shouldIndex(actualColName)) {
            long existingTS = timestamps.get(primaryKey);
            timestamps.put(primaryKey, Math.max(existingTS, cell.timestamp()));
            addFields(cell, actualColName, fields, columnDefinition);
        }
    }

    private void addPartitionKeyFields(ByteBuffer rowKey, Map<String, Long> timestamps, long timestamp, String primaryKey, List<Field> fields) {
        CType keyCType = tableMapper.cfMetaData.getKeyValidatorAsCType();
        Composite compoundRowKey = keyCType.fromByteBuffer(rowKey);
        for (Map.Entry<String, ColumnDefinition> entry : options.partitionKeysIndexed.entrySet()) {
            ByteBuffer value = compoundRowKey.get(entry.getValue().position());
            addKeyField(primaryKey, fields, timestamps, timestamp, entry, value);
        }
    }

    private void addClusteringKeyFields(CellName clusteringKey, Map<String, Long> timestamps, long timestamp, String primaryKey, List<Field> fields) {
        for (Map.Entry<String, ColumnDefinition> entry : options.clusteringKeysIndexed.entrySet()) {
            ByteBuffer value = clusteringKey.get(entry.getValue().position());
            addKeyField(primaryKey, fields, timestamps, timestamp, entry, value);
        }
    }

    private void addKeyField(String primaryKey, List<Field> fields, Map<String, Long> timestamps, long timestamp, Map.Entry<String, ColumnDefinition> entry, ByteBuffer value) {
        String keyColumnName = entry.getValue().name.toString();
        FieldType fieldType = options.fieldTypes.get(keyColumnName);
        long existingTS = timestamps.get(primaryKey);
        timestamps.put(primaryKey, Math.max(existingTS, timestamp));
        addField(fields, entry.getValue(), keyColumnName, fieldType, value);
        FieldType docValueType = options.fieldDocValueTypes.get(keyColumnName);
        if (docValueType != null)
            addField(fields, entry.getValue(), keyColumnName, docValueType, value);
    }


    protected List<Field> collectionFields(CollectionType validator, String colName, Cell column) {
        CellName cellName = column.name();
        List<Field> fields = new ArrayList<>();
        FieldType[] fieldTypesArr = options.collectionFieldTypes.get(colName);
        FieldType docValueType = options.collectionFieldDocValueTypes.get(colName);
        AbstractType keyType = validator.nameComparator();
        AbstractType valueType = validator.valueComparator();
        ByteBuffer collectionElement = cellName.collectionElement();
        if (validator instanceof MapType) {
            if (fieldTypesArr != null) {
                fields.add(Fields.field(colName + "._key", keyType, collectionElement, fieldTypesArr[0]));
                fields.add(Fields.field(colName + "._value", valueType, column.value(), fieldTypesArr[1]));
                fields.add(Fields.field((colName + "." + keyType.getString(collectionElement)).toLowerCase(), valueType, column.value(), fieldTypesArr[1]));
            }
            if (docValueType != null)
                fields.add(Fields.field((colName + "." + keyType.getString(collectionElement)).toLowerCase(), valueType, column.value(), docValueType));
        } else if (validator instanceof SetType) {
            if (fieldTypesArr != null)
                fields.add(Fields.field(colName, keyType, collectionElement, fieldTypesArr[0]));
            if (docValueType != null)
                fields.add(Fields.field(colName, keyType, collectionElement, docValueType));
        } else if (validator instanceof ListType) {
            if (fieldTypesArr != null)
                fields.add(Fields.field(colName, valueType, column.value(), fieldTypesArr[0]));
            if (docValueType != null)
                fields.add(Fields.field(colName, valueType, column.value(), docValueType));
        } else throw new UnsupportedOperationException("Unsupported collection type " + validator);

        return fields;
    }

    protected List<Field> idFields(DecoratedKey rowKey, String pkName, ByteBuffer pk) {
        return Arrays.asList(
                LuceneUtils.rkBytesDocValue(rowKey.getKey()),
                LuceneUtils.primaryKeyField(pkName),
                LuceneUtils.pkBytesDocValue(pk), LuceneUtils.pkNameDocValue(pkName),
                LuceneUtils.rowKeyIndexed(rowKeyString(rowKey)));
    }

    private String rowKeyString(DecoratedKey rowKey) {
        return tableMapper.primaryKeyAbstractType.getString(rowKey.getKey());
    }

    protected List<Field> tsFields(long ts) {
        Field tsField = LuceneUtils.tsField(ts, tsFieldType);
        return Arrays.asList(LuceneUtils.tsDocValues(ts), tsField);
    }


    protected void addField(List<Field> fields, ColumnDefinition columnDefinition, String name, FieldType fieldType, ByteBuffer value) {
        if (fieldType != null) {
            try {
                Field field = Fields.field(name, columnDefinition.type, value, fieldType);
                fields.add(field);
            } catch (Exception e) {
                logger.warn("Could not index column {}{}", columnDefinition, name);
                logger.warn("Exception while indexing column", e);
            }
        }
    }

    protected void addFields(Cell column, String name, List<Field> fields, ColumnDefinition columnDefinition) {
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
            addField(fields, columnDefinition, name, fieldType, column.value());
            FieldType docValueType = options.fieldDocValueTypes.get(name);
            if (docValueType != null)
                addField(fields, columnDefinition, name, docValueType, column.value());
        }
    }


}
