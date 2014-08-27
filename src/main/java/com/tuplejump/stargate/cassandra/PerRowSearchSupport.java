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
import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.lucene.SearcherCallback;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.cassandra.utils.Pair;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 * <p/>
 * A searcher which is used for searching on a RowIndex
 */
public class PerRowSearchSupport extends SearchSupport {

    protected Set<String> fieldNames;

    public PerRowSearchSupport(SecondaryIndexManager indexManager, RowIndex currentIndex, Set<ByteBuffer> columns, ByteBuffer primaryColName, Options options) {
        super(indexManager, currentIndex, columns, primaryColName, options);
        this.fieldNames = options.fieldTypes.keySet();
    }

    @Override
    public List<Row> search(ExtendedFilter mainFilter) {
        try {
            List<IndexExpression> clause = mainFilter.getClause();
            if (logger.isDebugEnabled())
                logger.debug("All IndexExprs {}", clause);
            Pair<Query, org.apache.lucene.search.SortField[]> queryAndSort = getQuery(matchThisIndex(clause));
            //This is mainly to allow data ranges to occur on searches with range and data together.
            return getRows(mainFilter, queryAndSort, false);
        } catch (Exception e) {
            if (currentIndex.isMetaColumn()) {
                logger.error("Exception occurred while querying", e);
                Row row = getErrorRow(baseCfs, currentIndex, e);
                if (row != null) {
                    return Collections.singletonList(row);
                } else {
                    return Collections.EMPTY_LIST;
                }
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    protected List<Row> getRows(final ExtendedFilter filter, final Pair<Query, org.apache.lucene.search.SortField[]> query, final boolean needsFiltering) {
        final SearchSupport searchSupport = this;
        SearcherCallback<List<Row>> sc = new SearcherCallback<List<Row>>() {
            @Override
            public List<Row> doWithSearcher(org.apache.lucene.search.IndexSearcher searcher) throws IOException {
                Utils.SimpleTimer timer = Utils.getStartedTimer(logger);
                List<Row> results;
                if (query == null) {
                    results = new ArrayList<>();
                } else {
                    Utils.SimpleTimer timer2 = Utils.getStartedTimer(SearchSupport.logger);
                    int maxResults = filter.maxRows();
                    int limit = searcher.getIndexReader().maxDoc();
                    if (limit == 0) {
                        limit = 1;
                    }
                    maxResults = Math.min(maxResults, limit);

                    //TopDocs topDocs = searcher.searchAfter(null, query.left, null, maxResults, new Sort(query.right), true, false);
                    IndexEntryCollector collector = new IndexEntryCollector(query.right, maxResults);
                    searcher.search(query.left, collector);
                    timer2.endLogTime("For TopDocs search for -" + collector.totalHits + " results");
                    if (SearchSupport.logger.isDebugEnabled()) {
                        SearchSupport.logger.debug(String.format("Search results [%s]", collector.totalHits));
                    }

                    ColumnFamilyStore.AbstractScanIterator iter = searchResultsIterator(searchSupport, baseCfs, searcher, filter, collector.docs().iterator(), needsFiltering);
                    //takes care of paging.
                    results = baseCfs.filter(iter, filter);
                }
                timer.endLogTime("SGIndex Search with results [" + results.size() + "]over all took -");
                return results;

            }
        };
        return currentIndex.search(filter, sc);
    }

    protected IndexExpression matchThisIndex(List<IndexExpression> clause) {
        for (IndexExpression expression : clause) {
            ColumnDefinition cfDef = baseCfs.metadata.getColumnDefinition(expression.column_name);
            String colName = CFDefinition.definitionType.getString(cfDef.name);
            //we only support Equal - Operators should be a part of the lucene query
            if (fieldNames.contains(colName) && expression.op == IndexOperator.EQ) {
                return expression;
            } else if (colName.equalsIgnoreCase(this.currentIndex.getPrimaryColumnName())) {
                return expression;
            }
        }
        return null;
    }


    @Override
    public boolean isIndexing(List<IndexExpression> clause) {
        IndexExpression expr = matchThisIndex(clause);
        return expr != null;
    }

    @Override
    protected ColumnFamilyStore.AbstractScanIterator searchResultsIterator(SearchSupport searchSupport, ColumnFamilyStore baseCfs, IndexSearcher searcher, ExtendedFilter filter, Iterator<IndexEntryCollector.IndexEntry> topDocs, boolean needsFiltering) throws IOException {
        return currentIndex.getScanIterator(searchSupport, baseCfs, searcher, filter, topDocs, needsFiltering);
    }

    @Override
    public boolean deleteIfNotLatest(DecoratedKey decoratedKey, long timestamp, String pkString, ColumnFamily cf) throws IOException {
        if (deleteRowIfNotLatest(decoratedKey, cf)) return true;
        Column lastColumn = null;
        for (ByteBuffer colKey : cf.getColumnNames()) {
            String name = currentIndex.getRowIndexSupport().getActualColumnName(colKey);
            Properties option = options.getFields().get(name);
            //if fieldType was not found then the column is not indexed
            if (option != null) {
                lastColumn = cf.getColumn(colKey);
            }
        }
        if (lastColumn != null && lastColumn.maxTimestamp() > timestamp) {
            currentIndex.delete(decoratedKey, pkString, timestamp);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteRowIfNotLatest(DecoratedKey decoratedKey, ColumnFamily cf) {
        if (!cf.getColumnNames().iterator().hasNext()) {
            if (currentIndex.getBaseCfs().metadata.getCfDef().iterator().hasNext())
                currentIndex.delete(decoratedKey);
            return true;
        }
        return false;
    }

    public static Row getErrorRow(ColumnFamilyStore table, RowIndex currentIndex, Exception e) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        int prefixSize = baseComparator.types.size() - (table.metadata.getCfDef().hasCollections ? 2 : 1);
        ColumnFamily cleanColumnFamily = TreeMapBackedSortedColumns.factory.create(table.metadata);
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


        List<ColumnDefinition> pkCols = table.metadata.partitionKeyColumns();
        for (ColumnDefinition columnDef : pkCols) {
            addDefaultColumn(table, columnDef, cleanColumnFamily, prefixSize, baseComparator);
        }
        List<ColumnDefinition> ckCols = table.metadata.clusteringKeyColumns();
        for (ColumnDefinition columnDef : ckCols) {
            addDefaultColumn(table, columnDef, cleanColumnFamily, prefixSize, baseComparator);
        }

        if (currentIndex.isMetaColumn()) {
            String indexColumnName = currentIndex.getPrimaryColumnName();
            Iterable<ColumnDefinition> cols = table.metadata.regularAndStaticColumns();
            for (ColumnDefinition columnDef : cols) {
                addDefaultColumn(table, columnDef, cleanColumnFamily, prefixSize, baseComparator);
            }
            addErrorColumn(table, indexColumnName, e.getMessage(), cleanColumnFamily);
            DecoratedKey dk = table.partitioner.decorateKey(partitionKey);
            return new Row(dk, cleanColumnFamily);
        } else {
            return null;
        }
    }

    public static void addDefaultColumn(ColumnFamilyStore table, ColumnDefinition col, ColumnFamily cleanColumnFamily, int prefixSize, CompositeType baseComparator) {
        CompositeType.Builder builder = baseComparator.builder();
        for (int i = 0; i < prefixSize; i++)
            builder.add(Fields.defaultValue(baseComparator.types.get(i)));
        builder.add(col.name);
        ByteBuffer finalColumnName = builder.build();
        Column defaultColumn = new Column(finalColumnName, Fields.defaultValue(col.getValidator()));
        cleanColumnFamily.addColumn(defaultColumn);
    }

    public static void addErrorColumn(ColumnFamilyStore table, String colName, String errorMsg, ColumnFamily cleanColumnFamily) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        int prefixSize = baseComparator.types.size() - (table.metadata.getCfDef().hasCollections ? 2 : 1);
        CompositeType.Builder builder = baseComparator.builder();
        for (int i = 0; i < prefixSize; i++)
            builder.add(Fields.defaultValue(baseComparator.types.get(i)));
        builder.add(UTF8Type.instance.decompose(colName));
        ByteBuffer finalColumnName = builder.build();
        Column scoreColumn = new Column(finalColumnName, UTF8Type.instance.decompose("{\"error\":\"" + StringEscapeUtils.escapeEcmaScript(errorMsg) + "\"}"));
        cleanColumnFamily.addColumn(scoreColumn);
    }
}
