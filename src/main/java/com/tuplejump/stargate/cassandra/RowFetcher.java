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
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.CellNameType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

/**
 * User: satya
 */
public class RowFetcher {
    protected static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);
    ResultMapper resultMapper;
    int columnsCount;
    int limit;
    ColumnFamilyStore table;

    public RowFetcher(ResultMapper resultMapper) throws Exception {
        this.resultMapper = resultMapper;
        this.limit = resultMapper.limit;
        this.table = resultMapper.tableMapper.table;
    }

    public List<Row> fetchRows() throws IOException {
        List<Row> rows = new ArrayList<>();
        TreeMultimap<DecoratedKey, IndexEntryCollector.IndexEntry> docs = resultMapper.docsByRowKey();

        for (DecoratedKey dk : docs.keySet()) {
            NavigableSet<IndexEntryCollector.IndexEntry> entries = docs.get(dk);

            if (!resultMapper.filter.dataRange.contains(dk)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Skipping entry {} outside of assigned scan range", dk.getToken());
                }
                continue;
            }
            final Map<CellName, ColumnFamily> fullSlice = resultMapper.fetchRangeSlice(entries, dk);

            for (IndexEntryCollector.IndexEntry input : entries) {
                CellName cellName = input.clusteringKey;
                if (!resultMapper.filter.columnFilter(dk.getKey()).maySelectPrefix(table.getComparator(), cellName.start())) {
                    continue;
                }
                ColumnFamily data = fullSlice.get(cellName);
                if (data == null || resultMapper.searchSupport.deleteIfNotLatest(dk, data.maxTimestamp(), input.pkName, data))
                    continue;
                float score = input.score;
                ColumnFamily cleanColumnFamily = resultMapper.showScore ? scored(score, data) : data;
                rows.add(new Row(dk, cleanColumnFamily));
                columnsCount++;
                if (columnsCount > limit) break;
            }
            if (columnsCount > limit) break;
        }

        return rows;
    }


    private ColumnFamily scored(Float score, ColumnFamily data) {
        ColumnFamily cleanColumnFamily = data.getFactory().create(table.metadata);
        String indexColumnName = resultMapper.tableMapper.primaryColumnName();
        boolean metaColReplaced = false;
        Cell firstColumn = null;
        for (Cell column : data) {
            if (firstColumn == null) firstColumn = column;
            ColumnIdentifier cellName = column.name().cql3ColumnName(table.metadata);
            String thisColName = cellName.toString();
            boolean isIndexColumn = indexColumnName.equals(thisColName);
            if (isIndexColumn) {
                Cell scoreColumn = new BufferCell(column.name(), UTF8Type.instance.decompose("{\"score\":" + score.toString() + "}"));
                cleanColumnFamily.addColumn(scoreColumn);
                metaColReplaced = true;
            } else {
                cleanColumnFamily.addColumn(column);
            }
        }
        if (!metaColReplaced && firstColumn != null) {
            Cell newColumn = getMetaColumn(firstColumn, score);
            cleanColumnFamily.addColumn(newColumn);
        }
        return cleanColumnFamily;
    }

    protected Cell getMetaColumn(Cell firstColumn, Float score) {
        CellNameType cellNameType = table.getComparator();
        ColumnDefinition columnDefinition = resultMapper.tableMapper.primaryColumnDefinition;
        CellName cellName = cellNameType.create(firstColumn.name(), columnDefinition);
        return new BufferCell(cellName, UTF8Type.instance.decompose("{\"score\":" + score.toString() + "}"));
    }

}
