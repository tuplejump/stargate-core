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

import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.lucene.IndexEntryCollector;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.SearcherCallback;
import com.tuplejump.stargate.lucene.query.Search;
import com.tuplejump.stargate.lucene.query.function.AggregateFunction;
import com.tuplejump.stargate.lucene.query.function.Function;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * User: satya
 * <p/>
 * A searcher which can be used with a SGIndex
 * Includes features to make lucene queries etc.
 */
public class SearchSupport extends SecondaryIndexSearcher {

    public static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);

    protected RowIndex currentIndex;

    protected Options options;

    protected Set<String> fieldNames;

    protected CustomColumnFactory customColumnFactory;

    public SearchSupport(SecondaryIndexManager indexManager, RowIndex currentIndex, Set<ByteBuffer> columns, Options options) {
        super(indexManager, columns);
        this.options = options;
        this.currentIndex = currentIndex;
        this.fieldNames = options.fieldTypes.keySet();
        this.customColumnFactory = new CustomColumnFactory();

    }


    protected Search getQuery(IndexExpression predicate) throws Exception {
        ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(predicate.column_name);
        String predicateValue = cd.getValidator().getString(predicate.bufferForValue());
        String columnName = CassandraUtils.getColumnName(cd);
        if (logger.isDebugEnabled())
            logger.debug("Index Searcher - query - predicate value [" + predicateValue + "] column name [" + columnName + "]");
        logger.debug("Column name is {}", columnName);
        return Search.fromJson(predicateValue);
    }


    @Override
    public List<Row> search(ExtendedFilter mainFilter) {

        List<IndexExpression> clause = mainFilter.getClause();
        if (logger.isDebugEnabled())
            logger.debug("All IndexExprs {}", clause);
        try {
            Search search = getQuery(matchThisIndex(clause));
            return getRows(mainFilter, search);
        } catch (Exception e) {
            if (currentIndex.isMetaColumn()) {
                logger.error("Exception occurred while querying", e);
                ByteBuffer errorMsg = UTF8Type.instance.decompose("{\"error\":\"" + StringEscapeUtils.escapeEcmaScript(e.getMessage()) + "\"}");
                Row row = customColumnFactory.getRowWithMetaColumn(baseCfs, currentIndex, errorMsg);
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

    protected List<Row> getRows(final ExtendedFilter filter, final Search search) {
        final SearchSupport searchSupport = this;
        AbstractBounds<RowPosition> keyRange = filter.dataRange.keyRange();
        final Range<Token> filterRange = new Range<>(keyRange.left.getToken(), keyRange.right.getToken());
        final boolean isSingleToken = filterRange.left.equals(filterRange.right);
        final boolean isFullRange = isSingleToken && baseCfs.partitioner.getMinimumToken().equals(filterRange.left);

        SearcherCallback<List<Row>> sc = new SearcherCallback<List<Row>>() {
            @Override
            public List<Row> doWithSearcher(org.apache.lucene.search.IndexSearcher searcher) throws Exception {
                Utils.SimpleTimer timer = Utils.getStartedTimer(logger);
                List<Row> results;
                if (search == null) {
                    results = new ArrayList<>();
                } else {
                    Utils.SimpleTimer timer2 = Utils.getStartedTimer(SearchSupport.logger);
                    Function function = search.function(options);
                    Query query = search.query(options);
                    int resultsLimit = searcher.getIndexReader().maxDoc();
                    if (function.shouldLimit()) {
                        if (resultsLimit == 0) {
                            resultsLimit = 1;
                        }
                        resultsLimit = Math.min(filter.currentLimit() + 1, resultsLimit);
                    }
                    function.init(options);
                    IndexEntryCollector collector = new IndexEntryCollector(function, search, options, resultsLimit);
                    searcher.search(query, collector);
                    timer2.endLogTime("TopDocs search for [" + collector.getTotalHits() + "] results ");
                    if (SearchSupport.logger.isDebugEnabled()) {
                        SearchSupport.logger.debug(String.format("Search results [%s]", collector.getTotalHits()));
                    }
                    RowScanner iter = new RowScanner(searchSupport, baseCfs, filter, collector, function instanceof AggregateFunction ? false : search.isShowScore());
                    Utils.SimpleTimer timer3 = Utils.getStartedTimer(SearchSupport.logger);
                    results = function.process(iter, customColumnFactory, baseCfs, currentIndex);
                    timer3.endLogTime("Aggregation [" + collector.getTotalHits() + "] results");
                }
                timer.endLogTime("Search with results [" + results.size() + "] ");
                return results;

            }

            @Override
            public Range<Token> filterRange() {
                return filterRange;
            }

            @Override
            public boolean isSingleToken() {
                return isSingleToken;
            }

            @Override
            public boolean isFullRange() {
                return isFullRange;
            }
        };

        return currentIndex.search(sc);
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

    public boolean deleteIfNotLatest(DecoratedKey decoratedKey, long timestamp, String pkString, ColumnFamily cf) throws IOException {
        if (deleteRowIfNotLatest(decoratedKey, cf)) return true;
        Column lastColumn = null;
        for (ByteBuffer colKey : cf.getColumnNames()) {
            String name = currentIndex.getRowIndexSupport().getActualColumnName(colKey);
            com.tuplejump.stargate.lucene.Properties option = options.fields.get(name);
            //if option was not found then the column is not indexed
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

    public boolean deleteRowIfNotLatest(DecoratedKey decoratedKey, ColumnFamily cf) {
        if (!cf.getColumnNames().iterator().hasNext()) {
            if (currentIndex.getBaseCfs().metadata.getCfDef().iterator().hasNext())
                currentIndex.delete(decoratedKey);
            return true;
        }
        return false;
    }


}
