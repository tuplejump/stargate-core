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
import org.apache.cassandra.db.ArrayBackedSortedColumns;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CBuilder;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.filter.ColumnSlice;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.SliceQueryFilter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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


    private final Composite start(CellName cellName) {
        CBuilder builder = tableMapper.clusteringCType.builder();
        for (int i = 0; i < cellName.clusteringSize(); i++) {
            ByteBuffer component = cellName.get(i);
            builder.add(component);
        }
        return builder.build();
    }

    private final Composite end(CellName cellName) {
        return start(cellName).withEOC(Composite.EOC.END);
    }


    public Map<CellName, ColumnFamily> fetchRangeSlice(ArrayList<IndexEntryCollector.IndexEntry> entries, DecoratedKey dk) {
        ColumnSlice[] columnSlices = getColumnSlices(entries);
        SliceQueryFilter sliceQueryFilter = new SliceQueryFilter(columnSlices, false, Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter(dk, tableMapper.table.name, sliceQueryFilter, filter.timestamp);
        ColumnFamily columnFamily = tableMapper.table.getColumnFamily(queryFilter);
        return getRows(columnFamily);
    }


    public final Map<CellName, ColumnFamily> getRows(ColumnFamily columnFamily) {
        Map<CellName, ColumnFamily> columnFamilies = new LinkedHashMap<>();
        for (Cell cell : columnFamily) {
            CellName cellName = cell.name();
            CellName clusteringKey = tableMapper.extractClusteringKey(cellName);
            ColumnFamily rowColumnFamily = columnFamilies.get(clusteringKey);
            if (rowColumnFamily == null) {
                rowColumnFamily = ArrayBackedSortedColumns.factory.create(tableMapper.cfMetaData);
                columnFamilies.put(clusteringKey, rowColumnFamily);
            }
            rowColumnFamily.addColumn(cell);

        }
        return columnFamilies;
    }


    private final ColumnSlice[] getColumnSlices(ArrayList<IndexEntryCollector.IndexEntry> entries) {
        ColumnSlice[] columnSlices = new ColumnSlice[entries.size()];
        int i = 0;
        for (IndexEntryCollector.IndexEntry entry : entries) {
            CellName clusteringKey = makeClusteringKey(entry.primaryKey);
            Composite start = start(clusteringKey);
            Composite end = end(clusteringKey);
            ColumnSlice columnSlice = new ColumnSlice(start, end);
            columnSlices[i++] = columnSlice;

        }
        return columnSlices;
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


    public CellName makeClusteringKey(ByteBuffer primaryKey) {
        ByteBuffer clusteringKeyBuf = tableMapper.primaryKeyType.extractLastComponent(primaryKey);
        return tableMapper.clusteringCType.cellFromByteBuffer(clusteringKeyBuf);
    }

    private boolean isDroppedColumn(Cell c, CFMetaData meta) {
        Long droppedAt = meta.getDroppedColumns().get(c.name().cql3ColumnName(meta));
        return droppedAt != null && c.timestamp() <= droppedAt;
    }


}
