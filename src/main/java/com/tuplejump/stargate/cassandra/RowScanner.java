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

import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.*;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: satya
 * An iterator which reads the actual rows from Cassandra using the search results
 */
public class RowScanner extends ColumnFamilyStore.AbstractScanIterator {
    protected static final Logger logger = LoggerFactory.getLogger(RowScanner.class);
    ColumnFamilyStore table;
    ExtendedFilter filter;
    Iterator<IndexEntryCollector.IndexEntry> indexIterator;
    SearchSupport searchSupport;
    int limit;
    int columnsCount = 0;
    boolean showScore = false;

    public RowScanner(SearchSupport searchSupport, ColumnFamilyStore table, ExtendedFilter filter, Iterator<IndexEntryCollector.IndexEntry> indexIterator, boolean showScore) throws Exception {
        this.searchSupport = searchSupport;
        this.table = table;
        this.filter = filter;
        this.indexIterator = indexIterator;
        this.limit = filter.currentLimit();
        this.showScore = showScore;
    }

    @Override
    public boolean needsFiltering() {
        return false;
    }

    @Override
    protected Row computeNext() {
        DataRange range = filter.dataRange;
        SliceQueryFilter sliceQueryFilter = (SliceQueryFilter) filter.dataRange.columnFilter(ByteBufferUtil.EMPTY_BYTE_BUFFER);
        while (indexIterator.hasNext() && columnsCount < limit) {
            try {
                IndexEntryCollector.IndexEntry entry = indexIterator.next();
                String pkNameString = entry.pkName;
                ByteBuffer rowKey = entry.rowKey;
                long ts = entry.timestamp;
                float score = entry.score;

                Pair<DecoratedKey, IDiskAtomFilter> keyAndFilter = getFilterAndKey(rowKey, sliceQueryFilter);
                if (keyAndFilter == null) {
                    continue;
                }

                DecoratedKey dk = keyAndFilter.left;
                if (!range.contains(dk)) {
                    if (SearchSupport.logger.isTraceEnabled()) {
                        SearchSupport.logger.trace("Skipping entry {} outside of assigned scan range", dk.token);
                    }
                    continue;
                }

                if (SearchSupport.logger.isTraceEnabled()) {
                    SearchSupport.logger.trace("Returning index hit for {}", dk);
                }

                Row row = getRow(pkNameString, keyAndFilter.right, dk, ts, score);
                if (row == null) {
                    if (SearchSupport.logger.isTraceEnabled())
                        SearchSupport.logger.trace("Returned Row is null");
                    continue;
                }
                columnsCount++;
                return row;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return endOfData();
    }

    private Row getRow(String pkString, IDiskAtomFilter dataFilter, DecoratedKey dk, long ts, Float score) throws IOException {
        ColumnFamily data = table.getColumnFamily(new QueryFilter(dk, table.name, dataFilter, filter.timestamp));
        if (data == null || searchSupport.deleteIfNotLatest(dk, ts, pkString, data)) {
            return null;
        }
        boolean scored = searchSupport.currentIndex.isMetaColumn() && showScore;
        ColumnFamily cleanColumnFamily = scored ? scored(score, data) : data;
        return new Row(dk, cleanColumnFamily);
    }

    private ColumnFamily scored(Float score, ColumnFamily data) {
        ColumnFamily cleanColumnFamily = TreeMapBackedSortedColumns.factory.create(table.metadata);
        String indexColumnName = searchSupport.currentIndex.getPrimaryColumnName();
        boolean metaColReplaced = false;
        Column firstColumn = null;
        for (Column column : data) {
            if (firstColumn == null) firstColumn = column;
            String thisColName = searchSupport.currentIndex.getRowIndexSupport().getActualColumnName(column.name());
            boolean isIndexColumn = indexColumnName.equals(thisColName);
            if (isIndexColumn) {
                if (logger.isDebugEnabled())
                    logger.debug("Primary col name {}", UTF8Type.instance.compose(column.name()));
                Column scoreColumn = new Column(column.name(), UTF8Type.instance.decompose("{\"score\":" + score.toString() + "}"));
                cleanColumnFamily.addColumn(scoreColumn);
                metaColReplaced = true;
            } else {
                cleanColumnFamily.addColumn(column);
            }
        }
        if (!metaColReplaced && firstColumn != null) {
            Column newColumn = getMetaColumn(firstColumn, indexColumnName, score);
            cleanColumnFamily.addColumn(newColumn);
        }
        return cleanColumnFamily;
    }

    protected Column getMetaColumn(Column firstColumn, String colName, Float score) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        ByteBuffer[] components = baseComparator.split(firstColumn.name());
        int prefixSize = baseComparator.types.size() - (table.metadata.getCfDef().hasCollections ? 2 : 1);
        CompositeType.Builder builder = baseComparator.builder();
        for (int i = 0; i < prefixSize; i++)
            builder.add(components[i]);
        builder.add(UTF8Type.instance.decompose(colName));
        ByteBuffer finalColumnName = builder.build();
        return new Column(finalColumnName, UTF8Type.instance.decompose("{\"score\":" + score.toString() + "}"));
    }


    protected Pair<DecoratedKey, IDiskAtomFilter> getFilterAndKey(ByteBuffer primaryKey, SliceQueryFilter sliceQueryFilter) {
        ByteBuffer[] components = getCompositePKComponents(table, primaryKey);
        ByteBuffer rowKey = getRowKeyFromPKComponents(components);
        DecoratedKey dk = table.partitioner.decorateKey(rowKey);
        final CompositeType baseComparator = (CompositeType) table.getComparator();
        int prefixSize = baseComparator.types.size() - (table.metadata.getCfDef().hasCollections ? 2 : 1);

        CompositeType.Builder builder = baseComparator.builder();

        for (int i = 0; i < prefixSize; i++)
            builder.add(components[i + 1]);

        ByteBuffer start = builder.build();
        if (!sliceQueryFilter.maySelectPrefix(table.getComparator(), start)) return null;

        ArrayList<ColumnSlice> allSlices = new ArrayList<>();
        ColumnSlice dataSlice = new ColumnSlice(start, builder.buildAsEndOfRange());
        if (table.metadata.hasStaticColumns()) {
            ColumnSlice staticSlice = new ColumnSlice(ByteBufferUtil.EMPTY_BYTE_BUFFER, table.metadata.getStaticColumnNameBuilder().buildAsEndOfRange());
            allSlices.add(staticSlice);
        }
        allSlices.add(dataSlice);
        ColumnSlice[] slices = new ColumnSlice[allSlices.size()];
        allSlices.toArray(slices);
        IDiskAtomFilter dataFilter = new SliceQueryFilter(slices, false, Integer.MAX_VALUE, table.metadata.clusteringKeyColumns().size());
        return Pair.create(dk, dataFilter);
    }

    public static ByteBuffer[] getCompositePKComponents(ColumnFamilyStore baseCfs, ByteBuffer pk) {
        CompositeType baseComparator = (CompositeType) baseCfs.getComparator();
        return baseComparator.split(pk);
    }

    public static ByteBuffer getRowKeyFromPKComponents(ByteBuffer[] pkComponents) {
        return pkComponents[0];
    }

    @Override
    public void close() throws IOException {
        //no op
    }

}
