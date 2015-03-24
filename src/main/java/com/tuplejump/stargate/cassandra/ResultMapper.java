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

import com.tuplejump.stargate.lucene.IndexEntryCollector;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CBuilder;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.filter.QueryFilter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: satya
 * A mapper which helps in reading the actual rows from Cassandra using the search results
 */
public class ResultMapper {
    public final ExtendedFilter filter;
    public final IndexEntryCollector collector;
    public final int limit;
    public final boolean showScore;
    public final TableMapper tableMapper;
    public final SearchSupport searchSupport;

    public ResultMapper(TableMapper tableMapper, SearchSupport searchSupport, ExtendedFilter filter, IndexEntryCollector collector, boolean showScore) throws Exception {
        this.tableMapper = tableMapper;
        this.searchSupport = searchSupport;
        this.filter = filter;
        this.collector = collector;
        this.limit = filter.currentLimit();
        this.showScore = showScore;
    }

    public ColumnFamily fetchSingleRow(DecoratedKey dk, ColumnFamily fullSlice, CellName cellName) {
        QueryFilter queryFilter = QueryFilter.getSliceFilter(dk, tableMapper.table.name, start(cellName), end(cellName), false, Integer.MAX_VALUE, filter.timestamp);
        return tableMapper.filterColumnFamily(fullSlice, queryFilter);
    }


    public final Composite start(CellName cellName) {
        CBuilder builder = tableMapper.clusteringCType.builder();
        for (int i = 0; i < cellName.clusteringSize(); i++) {
            ByteBuffer component = cellName.get(i);
            builder.add(component);
        }
        return builder.build();
    }

    public final Composite end(CellName cellName) {
        return start(cellName).withEOC(Composite.EOC.END);
    }


    public ColumnFamily fetchRangeSlice(ArrayList<IndexEntryCollector.IndexEntry> entries, DecoratedKey dk) {
        IndexEntryCollector.IndexEntry first = entries.get(0);
        IndexEntryCollector.IndexEntry last = entries.get(entries.size() - 1);
        CellName firstCellName = clusteringKey(first.primaryKey);
        CellName lastCellName = clusteringKey(last.primaryKey);
        QueryFilter sliceQueryFilter = QueryFilter.getSliceFilter(dk, tableMapper.table.name, start(firstCellName), end(lastCellName), false, Integer.MAX_VALUE, filter.timestamp);
        return tableMapper.table.getColumnFamily(sliceQueryFilter);
    }

    public void removeDroppedColumns(ColumnFamily cf) {
        CFMetaData metadata = cf.metadata();
        if (metadata.getDroppedColumns().isEmpty())
            return;

        Iterator<Cell> iter = cf.iterator();
        while (iter.hasNext())
            if (isDroppedColumn(iter.next(), metadata)) {
                iter.remove();
            }
    }


    public CellName clusteringKey(ByteBuffer primaryKey) {
        ByteBuffer clusteringKeyBuf = tableMapper.primaryKeyType.extractLastComponent(primaryKey);
        return tableMapper.clusteringCType.cellFromByteBuffer(clusteringKeyBuf);
    }

    private boolean isDroppedColumn(Cell c, CFMetaData meta) {
        Long droppedAt = meta.getDroppedColumns().get(c.name().cql3ColumnName(meta));
        return droppedAt != null && c.timestamp() <= droppedAt;
    }


}
