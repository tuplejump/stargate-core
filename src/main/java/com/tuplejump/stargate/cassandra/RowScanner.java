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
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.filter.IDiskAtomFilter;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * User: satya
 * An iterator which reads the actual rows from Cassandra using the search results
 */
public abstract class RowScanner extends ColumnFamilyStore.AbstractScanIterator {
    protected static final Logger logger = LoggerFactory.getLogger(RowScanner.class);
    ColumnFamilyStore table;
    org.apache.lucene.search.IndexSearcher searcher;
    ExtendedFilter filter;
    Iterator<IndexEntryCollector.IndexEntry> indexIterator;
    boolean needsFiltering;
    SearchSupport searchSupport;

    public RowScanner(SearchSupport searchSupport, ColumnFamilyStore table, IndexSearcher searcher, ExtendedFilter filter, Iterator<IndexEntryCollector.IndexEntry> indexIterator, boolean needsFiltering) throws Exception {
        this.searchSupport = searchSupport;
        this.table = table;
        this.searcher = searcher;
        this.filter = filter;
        this.needsFiltering = needsFiltering;
        this.indexIterator = indexIterator;
    }

    @Override
    public boolean needsFiltering() {
        return needsFiltering;
    }

    @Override
    protected Row computeNext() {
        DataRange range = filter.dataRange;
        SliceQueryFilter sliceQueryFilter = (SliceQueryFilter) filter.dataRange.columnFilter(ByteBufferUtil.EMPTY_BYTE_BUFFER);
        while (indexIterator.hasNext()) {
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
        ColumnFamily cleanColumnFamily = data;
        if (searchSupport.currentIndex.isMetaColumn()) {
            String indexColumnName = searchSupport.currentIndex.getPrimaryColumnName();
            cleanColumnFamily = TreeMapBackedSortedColumns.factory.create(table.metadata);
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
        }
        return new Row(dk, cleanColumnFamily);
    }


    protected abstract Column getMetaColumn(Column firstColumn, String colName, Float score);

    protected abstract Pair<DecoratedKey, IDiskAtomFilter> getFilterAndKey(ByteBuffer primaryKey, SliceQueryFilter sliceQueryFilter);

    @Override
    public void close() throws IOException {
        //no op
    }

}
