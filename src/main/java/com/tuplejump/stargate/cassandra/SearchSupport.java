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
import com.tuplejump.stargate.lucene.LuceneUtils;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.SearcherCallback;
import com.tuplejump.stargate.lucene.query.Search;
import com.tuplejump.stargate.lucene.query.function.Function;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.Operator;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.composites.BoundedComposite;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
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

    protected TableMapper tableMapper;

    protected Options options;

    protected Set<String> fieldNames;

    public SearchSupport(SecondaryIndexManager indexManager, RowIndex currentIndex, Set<ByteBuffer> columns, Options options) {
        super(indexManager, columns);
        this.options = options;
        this.currentIndex = currentIndex;
        this.fieldNames = options.fieldTypes.keySet();
        this.tableMapper = currentIndex.getTableMapper();
    }

    protected Search getQuery(String queryString) throws Exception {
        return Search.fromJson(queryString);
    }

    protected String getQueryString(IndexExpression predicate) throws Exception {
        ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(predicate.column);
        String predicateValue = cd.type.getString(predicate.value);
        if (logger.isDebugEnabled()) {
            String columnName = cd.name.toString();
            logger.debug("Index Searcher - query - predicate value [" + predicateValue + "] column name [" + columnName + "]");
            logger.debug("Column name is {}", columnName);
        }
        return predicateValue;
    }


    @Override
    public List<Row> search(ExtendedFilter mainFilter) {
        List<IndexExpression> clause = mainFilter.getClause();
        if (logger.isDebugEnabled())
            logger.debug("All IndexExprs {}", clause);
        try {
            String queryString = getQueryString(matchThisIndex(clause));
            Search search = getQuery(queryString);
            return getRows(mainFilter, search, queryString);
        } catch (Exception e) {
            logger.error("Exception occurred while querying", e);
            if (tableMapper.isMetaColumn) {
                ByteBuffer errorMsg = UTF8Type.instance.decompose("{\"error\":\"" + StringEscapeUtils.escapeEcmaScript(e.getMessage()) + "\"}");
                Row row = tableMapper.getRowWithMetaColumn(errorMsg);
                if (row != null) {
                    return Collections.singletonList(row);
                }
            }
            return Collections.EMPTY_LIST;
        }
    }

    protected List<Row> getRows(final ExtendedFilter filter, final Search search, final String queryString) {
        final SearchSupport searchSupport = this;
        AbstractBounds<RowPosition> keyRange = filter.dataRange.keyRange();
        final Range<Token> filterRange = new Range<>(keyRange.left.getToken(), keyRange.right.getToken());
        final boolean isSingleToken = filterRange.left.equals(filterRange.right);
        final boolean isFullRange = isSingleToken && baseCfs.partitioner.getMinimumToken().equals(filterRange.left);

        SearcherCallback<List<Row>> sc = new SearcherCallback<List<Row>>() {
            @Override
            public List<Row> doWithSearcher(org.apache.lucene.search.IndexSearcher searcher) throws Exception {
                Utils.SimpleTimer timer = Utils.getStartedTimer(logger);
                List<Row> results = null;
                if (search == null) {
                    results = Collections.EMPTY_LIST;
                } else {
                    Utils.SimpleTimer timer2 = Utils.getStartedTimer(SearchSupport.logger);
                    Function function = search.function();
                    Query query = LuceneUtils.getQueryUpdatedWithPKCondition(search.query(options), getPartitionKeyString(filter));
                    int resultsLimit = filter.currentLimit();
                    if (resultsLimit == 0) {
                        resultsLimit = 1;
                    }

                    function.init(options);
                    final boolean reverseClustering = filter.dataRange.columnFilter(null).isReversed();
                    if (logger.isWarnEnabled()) {
                        logger.warn("Current limit " + filter.currentLimit() + " and reverse is " + reverseClustering);
                    }
                    FieldDoc afterDoc = getAfterDoc(searcher, reverseClustering, filter, search);
                    IndexEntryCollector collector = new IndexEntryCollector(afterDoc, reverseClustering, tableMapper, search, options, resultsLimit);
                    searcher.search(query, collector);
                    timer2.endLogTime("Lucene search for [" + collector.getTotalHits() + "] results ");
                    if (SearchSupport.logger.isDebugEnabled()) {
                        SearchSupport.logger.debug(String.format("Search results [%s]", collector.getTotalHits()));
                    }
                    ResultMapper resultMapper = new ResultMapper(tableMapper, searchSupport, filter, collector, function.shouldTryScoring() && search.isShowScore(), reverseClustering);
                    Utils.SimpleTimer timer3 = Utils.getStartedTimer(SearchSupport.logger);
                    results = function.process(resultMapper, baseCfs, currentIndex);
                    timer3.endLogTime("Aggregation [" + results.size() + "] results");
                    timer.endLogTime("Search with results [" + results.size() + "] ");
                }

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

    private FieldDoc getAfterDoc(IndexSearcher searcher, boolean reverseClustering, ExtendedFilter filter, Search search) throws IOException {
        FieldDoc afterDoc = null;
        if (isPagingQuery(filter.dataRange) && filter.dataRange.keyRange().left instanceof DecoratedKey) {
            DataRange.Paging paging = (DataRange.Paging) filter.dataRange;
            DecoratedKey dk = (DecoratedKey) filter.dataRange.keyRange().left;
            ByteBuffer afterPK = getPageStart(dk, paging);
            if (logger.isWarnEnabled()) {
                logger.warn("Paged query - After PK is " + afterPK);
            }
            if (afterPK != null) {
                String pk = tableMapper.primaryKeyType.getString(afterPK);
                Sort sort = new Sort(search.primaryKeySort(tableMapper, reverseClustering));
                TopFieldDocs docs = searcher.search(new TermQuery(LuceneUtils.primaryKeyTerm(pk)), 1, sort);
                afterDoc = (FieldDoc) docs.scoreDocs[0];
                if (logger.isWarnEnabled()) {
                    logger.warn("After Doc is " + afterDoc.doc + " and pk is " + pk);
                }
            }
        }
        return afterDoc;
    }

    protected IndexExpression matchThisIndex(List<IndexExpression> clause) {
        for (IndexExpression expression : clause) {
            ColumnDefinition cfDef = baseCfs.metadata.getColumnDefinition(expression.column);
            String colName = cfDef.name.toString();
            //we only support Equal - Operators should be a part of the lucene query
            if (fieldNames.contains(colName) && expression.operator == Operator.EQ) {
                return expression;
            } else if (colName.equalsIgnoreCase(tableMapper.primaryColumnName())) {
                return expression;
            }
        }
        return null;
    }

    protected String getPartitionKeyString(ExtendedFilter mainFilter) {
        AbstractBounds<RowPosition> keyRange = mainFilter.dataRange.keyRange();
        if (keyRange != null && keyRange.left != null && keyRange.left instanceof DecoratedKey) {
            DecoratedKey left = (DecoratedKey) keyRange.left;
            if (keyRange.right instanceof DecoratedKey && left.equals(keyRange.right)) {
                return tableMapper.primaryKeyAbstractType.getString(left.getKey());
            }
        }
        return null;
    }

    private ByteBuffer getPageStart(DecoratedKey dk, DataRange.Paging pageRange) {
        try {
            Composite start = (Composite) getPrivateProperty(pageRange, "firstPartitionColumnStart");
            Composite end = (Composite) getPrivateProperty(pageRange, "lastPartitionColumnFinish");
            if (start == null || start.isEmpty() || start instanceof BoundedComposite) {
                return null;
            }
            CellName clusteringKey = tableMapper.extractClusteringKey(start);
            if (end == null || end.isEmpty() || end instanceof BoundedComposite) {
                return tableMapper.primaryKey(dk.getKey(), clusteringKey);
            }
            CellName endKey = tableMapper.extractClusteringKey(end);
            if (!clusteringKey.isSameCQL3RowAs(tableMapper.clusteringCType, endKey)) {
                return tableMapper.primaryKey(dk.getKey(), clusteringKey);
            }

        } catch (NoSuchFieldException e) {
            //do nothing;
        } catch (IllegalAccessException e) {
            //do nothing
        }
        return null;
    }

    private boolean isPagingQuery(DataRange dataRange) {
        return (dataRange instanceof DataRange.Paging);
    }


    private Object getPrivateProperty(Object instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    public boolean deleteIfNotLatest(DecoratedKey decoratedKey, long timestamp, String pkString, ColumnFamily cf) throws IOException {
        if (deleteRowIfNotLatest(decoratedKey, cf)) return true;
        Cell lastColumn = null;
        for (CellName colKey : cf.getColumnNames()) {
            String name = colKey.cql3ColumnName(tableMapper.cfMetaData).toString();
            com.tuplejump.stargate.lucene.Properties option = options.fields.get(name);
            //if option was not found then the column is not indexed
            if (option != null) {
                lastColumn = cf.getColumn(colKey);
            }
        }
        if (lastColumn != null && lastColumn.timestamp() > timestamp) {
            currentIndex.delete(decoratedKey, pkString, timestamp);
            return true;
        }
        return false;
    }

    public boolean deleteRowIfNotLatest(DecoratedKey decoratedKey, ColumnFamily cf) {
        if (!cf.getColumnNames().iterator().hasNext()) {//no columns available
            currentIndex.deleteByKey(decoratedKey);
            return true;
        }
        return false;
    }

    @Override
    protected IndexExpression highestSelectivityPredicate(List<IndexExpression> clause, boolean includeInTrace) {
        return matchThisIndex(clause);
    }


    @Override
    public boolean canHandleIndexClause(List<IndexExpression> clause) {
        return matchThisIndex(clause) != null;
    }

    public Options getOptions() {
        return options;
    }
}
