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

import com.google.common.collect.TreeMultimap;
import com.tuplejump.stargate.lucene.IndexEntryCollector;
import com.tuplejump.stargate.lucene.IndexEntryCollector.IndexEntry;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.filter.ColumnSlice;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: satya
 * A mapper which helps in reading the actual rows from Cassandra using the search results
 */
public class ResultMapper {

    public static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);
    public final ExtendedFilter filter;
    public final IndexEntryCollector collector;
    public final int limit;
    public final boolean showScore;
    public final TableMapper tableMapper;
    public final SearchSupport searchSupport;
    public final boolean reverseSort;

    public ResultMapper(TableMapper tableMapper, SearchSupport searchSupport, ExtendedFilter filter, IndexEntryCollector collector, boolean showScore, boolean reverseSort) throws Exception {
        this.tableMapper = tableMapper;
        this.searchSupport = searchSupport;
        this.filter = filter;
        this.collector = collector;
        this.limit = filter.currentLimit();
        this.showScore = showScore;
        this.reverseSort = reverseSort;
    }


    public Map<CellName, ColumnFamily> fetchRangeSlice(Collection<IndexEntry> entries, DecoratedKey dk, boolean reversed) {
        return getCellNameColumnFamilyMap(dk, getColumnSlices(entries), reversed);
    }


    public TreeMultimap docsByRowKey() {
        return collector.docsByRowKey();
    }

    public List<IndexEntry> docs() {
        return collector.docs();
    }


    private ColumnSlice[] getColumnSlices(Collection<IndexEntry> entries) {
        ColumnSlice[] columnSlices = new ColumnSlice[entries.size()];
        int i = 0;
        for (IndexEntry entry : entries) {
            Composite start = tableMapper.start(entry.clusteringKey());
            Composite end = tableMapper.end(start);
            ColumnSlice columnSlice = new ColumnSlice(start, end);
            columnSlices[i++] = columnSlice;

        }
        return columnSlices;
    }

    public Map<CellName, ColumnFamily> fetchPagedRangeSlice(Collection<IndexEntry> entries, DecoratedKey dk, int pageSize, boolean reversed) {
        return getCellNameColumnFamilyMap(dk, getPagedColumnSlices(dk, entries, pageSize), reversed);
    }

    private ColumnSlice[] getPagedColumnSlices(DecoratedKey dk, Collection<IndexEntry> entries, int pageSize) {
        ArrayList<ColumnSlice> columnSlices = new ArrayList<>(Math.min(entries.size(), pageSize));
        for (IndexEntry entry : entries) {
            CellName cellName = entry.clusteringKey();
            if (!filter.columnFilter(dk.getKey()).maySelectPrefix(tableMapper.table.getComparator(), cellName.start())) {
                continue;
            }
            Composite start = tableMapper.start(cellName);
            Composite end = tableMapper.end(start);
            ColumnSlice columnSlice = new ColumnSlice(start, end);
            columnSlices.add(columnSlice);
            if (columnSlices.size() == pageSize) {
                break;
            }
        }
        return columnSlices.toArray(new ColumnSlice[columnSlices.size()]);
    }

    private Map<CellName, ColumnFamily> getCellNameColumnFamilyMap(DecoratedKey dk, ColumnSlice[] columnSlices, boolean reversed) {
        SliceQueryFilter sliceQueryFilter = new SliceQueryFilter(columnSlices, reversed, Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter(dk, tableMapper.table.name, sliceQueryFilter, filter.timestamp);
        ColumnFamily columnFamily = tableMapper.table.getColumnFamily(queryFilter);
        return tableMapper.getRows(columnFamily);
    }


}
