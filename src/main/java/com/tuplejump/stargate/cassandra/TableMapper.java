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
import com.tuplejump.stargate.lucene.LuceneUtils;
import com.tuplejump.stargate.lucene.query.function.Tuple;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.composites.*;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: satya
 */
public class TableMapper {

    public final AbstractType primaryKeyAbstractType;
    public final AbstractType clusteringKeyType;
    public final CompositeType primaryKeyType;
    public final CellNameType clusteringCType;
    public final ColumnFamilyStore table;
    public final ByteBuffer defaultPartitionKey;
    public final boolean isMetaColumn;
    public final ColumnDefinition primaryColumnDefinition;
    public final CFMetaData cfMetaData;

    public final SortField pkSortField = getPkSort(false);
    public final SortField pkSortFieldReverse = getPkSort(true);

    public final SortField tokenSortField = new SortField(LuceneUtils.TOKEN_LONG, SortField.Type.LONG, false);
    public final SortField tokenSortFieldReverse = new SortField(LuceneUtils.TOKEN_LONG, SortField.Type.LONG, true);
    {
        tokenSortField.setMissingValue(CassandraUtils.MINIMUM_TOKEN_VALUE);
        tokenSortFieldReverse.setMissingValue(CassandraUtils.MINIMUM_TOKEN_VALUE);
    }

    public TableMapper(ColumnFamilyStore table, boolean isMetaColumn, ColumnDefinition primaryColumnDefinition) {
        this.table = table;
        this.cfMetaData = table.metadata;
        this.clusteringCType = table.getComparator();
        this.primaryKeyAbstractType = table.metadata.getKeyValidator();
        this.clusteringKeyType = table.getComparator().asAbstractType();
        this.primaryKeyType = CompositeType.getInstance(primaryKeyAbstractType, clusteringKeyType);
        this.defaultPartitionKey = defaultPartitionKey();
        this.isMetaColumn = isMetaColumn;
        this.primaryColumnDefinition = primaryColumnDefinition;
    }

    private SortField getPkSort(final boolean reverseClustering) {
        final TableMapper tableMapper = this;
        FieldComparatorSource pkComparatorSource = new FieldComparatorSource() {
            @Override
            public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, final boolean reversed) throws IOException {
                return new FieldComparator.TermValComparator(numHits, fieldname, reversed) {
                    @Override
                    public int compareValues(BytesRef val1, BytesRef val2) {
                        if (val1 == null || val2 == null) {
                            return super.compareValues(val1, val2);
                        } else {
                            return tableMapper.primaryKeyType.compare(fromBytesRef(val1), fromBytesRef(val2));
                        }
                    }
                };
            }
        };
        return new SortField(LuceneUtils.PK_BYTES, pkComparatorSource, reverseClustering);
    }

    public static ByteBuffer fromBytesRef(BytesRef ref) {
        return ByteBuffer.wrap(ref.bytes, ref.offset, ref.length);
    }

    public String primaryColumnName() {
        return primaryColumnDefinition.name.toString();
    }

    public DecoratedKey decorateKey(ByteBuffer rowKey) {
        return table.partitioner.decorateKey(rowKey);
    }

    public void load(Map<String, Integer> positions, Tuple tuple, Row row) {
        ColumnFamily cf = row.cf;
        ByteBuffer rowKey = row.key.getKey();

        Collection<Cell> cols = cf.getSortedColumns();
        boolean keyColumnsAdded = false;
        for (Cell column : cols) {
            if (!keyColumnsAdded) {
                addKeyColumns(positions, tuple, rowKey);
                keyColumnsAdded = true;
            }
            String actualColumnName = column.name().cql3ColumnName(table.metadata).toString();
            ByteBuffer colValue = column.value();
            AbstractType<?> valueValidator = table.metadata.getValueValidator(column.name());
            if (valueValidator.isCollection()) {
                CollectionType validator = (CollectionType) valueValidator;
                AbstractType keyType = validator.nameComparator();
                AbstractType valueType = validator.valueComparator();
                ByteBuffer keyBuf = column.name().collectionElement();
                if (valueValidator instanceof MapType) {
                    actualColumnName = actualColumnName + "." + keyType.compose(keyBuf);
                    valueValidator = valueType;
                } else if (valueValidator instanceof SetType) {
                    colValue = keyBuf;
                    valueValidator = keyType;
                } else {
                    valueValidator = valueType;
                }
            }
            for (String field : positions.keySet()) {
                if (actualColumnName.equalsIgnoreCase(field)) {
                    tuple.getTuple()[positions.get(field)] = valueValidator.compose(colValue);
                }
            }
        }
    }

    private void addKeyColumns(Map<String, Integer> positions, Tuple tuple, ByteBuffer rowKey) {

        CType keyCType = table.metadata.getKeyValidatorAsCType();
        Composite compoundRowKey = keyCType.fromByteBuffer(rowKey);
        List<ColumnDefinition> partitionKeys = table.metadata.partitionKeyColumns();

        for (ColumnDefinition entry : partitionKeys) {
            ByteBuffer value = compoundRowKey.get(entry.position());
            String actualColumnName = entry.name.toString();
            for (String field : positions.keySet()) {
                if (actualColumnName.equalsIgnoreCase(field)) {
                    tuple.getTuple()[positions.get(field)] = entry.type.compose(value);
                }
            }
        }
    }


    public Row getRowWithMetaColumn(ByteBuffer metaColumnValue) {

        if (isMetaColumn) {
            ColumnFamily cleanColumnFamily = ArrayBackedSortedColumns.factory.create(table.metadata);
            CellNameType cellNameType = table.getComparator();
            boolean hasCollections = cellNameType.hasCollections();
            int prefixSize = cellNameType.size() - (hasCollections ? 2 : 1);
            CBuilder builder = cellNameType.builder();
            for (int i = 0; i < prefixSize; i++) {
                AbstractType<?> type = cellNameType.subtype(i);
                builder.add(Fields.defaultValue(type));
            }
            Composite prefix = builder.build();
            Iterable<ColumnDefinition> cols = table.metadata.regularAndStaticColumns();
            for (ColumnDefinition columnDef : cols) {
                if (columnDef.equals(primaryColumnDefinition)) {
                    addColumn(table, cleanColumnFamily, primaryColumnDefinition, prefix, metaColumnValue);
                } else {
                    addColumn(table, cleanColumnFamily, columnDef, prefix, Fields.defaultValue(columnDef.type));
                }
            }
            DecoratedKey dk = table.partitioner.decorateKey(defaultPartitionKey);
            return new Row(dk, cleanColumnFamily);

        } else {
            return null;
        }
    }


    private static void addColumn(ColumnFamilyStore table, ColumnFamily cleanColumnFamily, ColumnDefinition columnDefinition, Composite prefix, ByteBuffer metaColumnValue) {
        if (!columnDefinition.type.asCQL3Type().isCollection()) {
            CellNameType cellNameType = table.getComparator();
            CellName cellName = cellNameType.create(prefix, columnDefinition);
            Cell scoreColumn = new BufferCell(cellName, metaColumnValue);
            cleanColumnFamily.addColumn(scoreColumn);
        }
    }


    private ByteBuffer defaultPartitionKey() {
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
        return partitionKey;
    }

    public ByteBuffer primaryKey(ByteBuffer rowKey, Composite clusteringKey) {
        return primaryKeyType.builder().add(rowKey).add(clusteringKey.toByteBuffer()).build();
    }

    public CellName extractClusteringKey(Composite cellName) {
        int clusterColumns = table.metadata.clusteringColumns().size();
        Object[] components = new ByteBuffer[clusterColumns + 1];
        for (int i = 0; i < clusterColumns; i++) {
            components[i] = cellName.get(i);
        }
        components[clusterColumns] = ByteBufferUtil.EMPTY_BYTE_BUFFER;
        return clusteringCType.makeCellName(components);
    }

    public CellName makeClusteringKey(ByteBuffer primaryKey) {
        ByteBuffer clusteringKeyBuf = primaryKeyType.extractLastComponent(primaryKey);
        return clusteringCType.cellFromByteBuffer(clusteringKeyBuf);
    }

    public final Map<CellName, ColumnFamily> getRows(ColumnFamily columnFamily) {
        Map<CellName, ColumnFamily> columnFamilies = new LinkedHashMap<>();
        if (columnFamily == null)
            return columnFamilies;
        for (Cell cell : columnFamily) {
            CellName cellName = cell.name();
            CellName clusteringKey = extractClusteringKey(cellName);
            ColumnFamily row = columnFamilies.get(clusteringKey);

            if (row == null) {
                row = ArrayBackedSortedColumns.factory.create(cfMetaData);
                columnFamilies.put(clusteringKey, row);
            }
            if (!isDroppedColumn(cell, cfMetaData)) {
                row.addColumn(cell);
            }

        }
        return columnFamilies;
    }

    public final Composite start(CellName cellName) {
        CBuilder builder = clusteringCType.builder();
        for (int i = 0; i < cellName.clusteringSize(); i++) {
            ByteBuffer component = cellName.get(i);
            builder.add(component);
        }
        return builder.build();
    }

    public final Composite end(Composite start) {
        return start.withEOC(Composite.EOC.END);
    }

    public boolean isDroppedColumn(Cell c, CFMetaData meta) {
        Long droppedAt = meta.getDroppedColumns().get(c.name().cql3ColumnName(meta));
        return droppedAt != null && c.timestamp() <= droppedAt;
    }


}
