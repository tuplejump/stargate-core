package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.luc.Indexer;
import com.tuplejump.stargate.luc.SearcherCallback;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.*;
import org.apache.cassandra.db.index.SecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.exceptions.ReadTimeoutException;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.HeapAllocator;
import org.apache.cassandra.utils.IFilter;
import org.apache.cassandra.utils.Pair;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 * <p/>
 * A searcher which can be used with a SGIndex
 */
public abstract class IndexSearcher extends SecondaryIndexSearcher {

    protected static final Logger logger = LoggerFactory.getLogger(IndexSearcher.class);
    Analyzer analyzer;
    Map<String, NumericConfig> numericConfigMap;
    final static EscapeQuerySyntax ESCAPER = new EscapeQuerySyntaxImpl();
    SecondaryIndex currentIndex;
    Indexer indexer;
    boolean isKeyWordCheck;
    ByteBuffer primaryColName;


    public IndexSearcher(SecondaryIndexManager indexManager, SecondaryIndex currentIndex, Indexer indexer, Set<ByteBuffer> columns, ByteBuffer primaryColName, Map<String, NumericConfig> numericConfigMap) {
        super(indexManager, columns);
        this.analyzer = indexer.getAnalyzer();
        this.numericConfigMap = numericConfigMap;
        this.currentIndex = currentIndex;
        this.indexer = indexer;
        isKeyWordCheck = indexer.getAnalyzer() instanceof KeywordAnalyzer;
        this.primaryColName = primaryColName;
    }


    protected List<Row> getRows(final ExtendedFilter filter, final Query query, final FilterChain chain, final boolean addlFilter) {
        SearcherCallback<List<Row>> sc = new SearcherCallback<List<Row>>() {
            @Override
            public List<Row> doWithSearcher(org.apache.lucene.search.IndexSearcher searcher) {
                try {
                    Utils.SimpleTimer timer = Utils.getStartedTimer(logger);
                    List<Row> results = searchResults(searcher, filter, query, chain, addlFilter);
                    timer.endLogTime("SGIndex Search with results [" + results.size() + "]over all took -");
                    return results;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        return indexer.search(sc);
    }

    public IFilter searchReturnFilter(final int maxResults, final Query query) {
        SearcherCallback sc = new SearcherCallback<IFilter>() {
            @Override
            public IFilter doWithSearcher(org.apache.lucene.search.IndexSearcher searcher) {
                try {
                    Utils.SimpleTimer timer = Utils.getStartedTimer(logger);
                    final BinaryDocValues rowKeyValues = Fields.getPKDocValues(searcher);
                    TopDocs topDocs = searcher.search(query, maxResults);
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(" *FILTER* Search results [%s]", topDocs.totalHits));
                    }
                    IFilter filter = new BitmapFilter();
                    addToFilter(filter, rowKeyValues, topDocs);
                    timer.endLogTime("SGIndex *FILTER* Search over all took -");
                    return filter;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        return (IFilter) indexer.search(sc);
    }

    protected void addToFilter(IFilter filter, BinaryDocValues rowKeyValues, TopDocs topDocs) throws IOException {
        ScoreDoc[] docs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : docs) {
            ByteBuffer rowKey = Fields.primaryKey(rowKeyValues, scoreDoc.doc);
            filter.add(rowKey);
        }
    }

    protected List<Row> searchResults(final org.apache.lucene.search.IndexSearcher searcher, final ExtendedFilter filter, Query query, final FilterChain chain, final boolean needsFiltering) throws IOException {
        int maxResults = filter.maxRows();
        final DataRange range = filter.dataRange;
        Utils.SimpleTimer timer = Utils.getStartedTimer(logger);
        final SortedDocValues rowKeyValues = Fields.getPKDocValues(searcher);
        final NumericDocValues tsValues = Fields.getTSDocValues(searcher);
        timer.endLogTime("For BinaryDocValues retrieval -");
        Utils.SimpleTimer timer2 = Utils.getStartedTimer(logger);
        if (query == null) {
            return Collections.EMPTY_LIST;
        }

        TopDocs topDocs = searcher.search(query, maxResults);

        timer2.endLogTime("For TopDocs search for -" + topDocs.totalHits + " results");
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Search results [%s]", topDocs.totalHits));
        }

        final ArrayIterator indexIterator = new ArrayIterator(topDocs.scoreDocs);
        final CFDefinition cfDef = baseCfs.metadata.getCfDef();

        ColumnFamilyStore.AbstractScanIterator iter = new ColumnFamilyStore.AbstractScanIterator() {

            @Override
            public boolean needsFiltering() {
                return needsFiltering;
            }

            @Override
            protected Row computeNext() {
                while (indexIterator.hasNext()) {
                    try {
                        ScoreDoc scoreDoc = (ScoreDoc) indexIterator.next();
                        ByteBuffer primaryKey = Fields.primaryKey(rowKeyValues, scoreDoc.doc);
                        if (chain != null && !chain.accepts(primaryKey)) {
                            continue;
                        }

                        Pair<DecoratedKey, IDiskAtomFilter> keyAndFilter = getFilterAndKey(primaryKey);
                        if (keyAndFilter == null)
                            continue;

                        DecoratedKey dk = keyAndFilter.left;
                        if (!range.contains(dk)) {
                            if (logger.isTraceEnabled())
                                logger.trace("Skipping entry {} outside of assigned scan range", dk.token);
                            continue;
                        }
                        if (logger.isTraceEnabled())
                            logger.trace("Returning index hit for {}", dk);

                        Row row = getRow(keyAndFilter.right, dk, scoreDoc, tsValues);
                        if (row == null) {
                            if (logger.isTraceEnabled())
                                logger.trace("Returned Row is null");
                            continue;
                        }
                        return row;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return endOfData();
            }

            private Row getRow(IDiskAtomFilter dataFilter, DecoratedKey dk, ScoreDoc scoreDoc, NumericDocValues tsValues) throws IOException {
                ColumnFamily data;
                if (cfDef.isComposite) {
                    data = baseCfs.getColumnFamily(new QueryFilter(dk, baseCfs.name, dataFilter, filter.timestamp));
                    if (data == null || checkIfNotLatestAndRemove(tsValues, scoreDoc.doc, searcher, dk.key, data)) {
                        return null;
                    }
                } else {
                    data = baseCfs.getColumnFamily(new QueryFilter(dk, baseCfs.name, dataFilter, filter.timestamp));
                    // While the column family we'll get in the end should contains the primary clause column, the initialFilter may not have found it and can thus be null
                    if (data == null)
                        data = TreeMapBackedSortedColumns.factory.create(baseCfs.metadata);

                    // as in CFS.filter - extend the filter to ensure we include the columns
                    // from the index expressions, just in case they weren't included in the initialFilter
                    IDiskAtomFilter extraFilter = filter.getExtraFilter(dk, data);
                    if (extraFilter != null) {
                        ColumnFamily cf = baseCfs.getColumnFamily(new QueryFilter(dk, baseCfs.name, extraFilter, filter.timestamp));
                        if (cf != null)
                            data.addAll(cf, HeapAllocator.instance);
                    }

                    if (checkIfNotLatestAndRemove(tsValues, scoreDoc.doc, searcher, dk.key, data)) {
                        return null;
                    }
                }
                return new Row(dk, data);
            }


            private Pair<DecoratedKey, IDiskAtomFilter> getFilterAndKey(ByteBuffer primaryKey) {
                DecoratedKey dk;
                IDiskAtomFilter dataFilter;
                if (cfDef.isComposite) {
                    ByteBuffer[] components = Utils.getCompositePKComponents(baseCfs, primaryKey);
                    ByteBuffer rowKey = Utils.getRowKeyFromPKComponents(components);
                    dk = baseCfs.partitioner.decorateKey(rowKey);
                    final CompositeType baseComparator = (CompositeType) baseCfs.getComparator();
                    int prefixSize = baseComparator.types.size() - (cfDef.hasCollections ? 2 : 1);

                    CompositeType.Builder builder = baseComparator.builder();

                    for (int i = 0; i < prefixSize; i++)
                        builder.add(components[i + 1]);

                    // Does this "row" match the user original filter
                    ByteBuffer start = builder.build();

                    ColumnSlice dataSlice = new ColumnSlice(start, builder.buildAsEndOfRange());
                    ColumnSlice[] slices;
                    if (baseCfs.metadata.hasStaticColumns()) {
                        ColumnSlice staticSlice = new ColumnSlice(ByteBufferUtil.EMPTY_BYTE_BUFFER, baseCfs.metadata.getStaticColumnNameBuilder().buildAsEndOfRange());
                        slices = new ColumnSlice[]{staticSlice, dataSlice};
                    } else {
                        slices = new ColumnSlice[]{dataSlice};
                    }
                    dataFilter = new SliceQueryFilter(slices, false, Integer.MAX_VALUE, baseCfs.metadata.clusteringKeyColumns().size());
                } else {
                    dk = baseCfs.partitioner.decorateKey(primaryKey);
                    dataFilter = filter.columnFilter(primaryKey);
                }
                return Pair.create(dk, dataFilter);
            }


            @Override
            public void close() throws IOException {
                //no op
            }
        };
        return baseCfs.filter(iter, filter);
    }


    protected Query getQuery(IndexExpression predicate) {
        ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(predicate.column_name);
        String predicateValue = cd.getValidator().getString(predicate.bufferForValue());
        String columnName = Utils.getColumnName(cd);
        if (logger.isDebugEnabled())
            logger.debug("Index Searcher - query - predicate value [" + predicateValue + "] column name [" + columnName + "]");
        if (Fields.isNumeric(cd.getValidator())) {
            predicateValue = ESCAPER.escape(predicateValue, Locale.US, EscapeQuerySyntax.Type.STRING).toString();
        }
        logger.debug("Column name is {}", columnName);
        try {
            StandardQueryParser parser = new StandardQueryParser(analyzer);
            parser.setNumericConfigMap(numericConfigMap);
            parser.setAllowLeadingWildcard(true);
            logger.debug("Numeric config is {}", parser.getNumericConfigMap());
            return parser.parse(predicateValue, columnName);
        } catch (QueryNodeException e) {
            logger.warn("Could not parse lucene query", e);
        }
        return null;
    }


    protected Pair<List<IndexSearcher>, List<IndexExpression>> matchOtherIndexes(List<IndexExpression> clause) {
        List<IndexExpression> matches = new ArrayList<>();
        List<IndexSearcher> searchers = new ArrayList<>();
        Collection<SecondaryIndex> indexes = indexManager.getIndexesNotBackedByCfs();
        for (SecondaryIndex index : indexes) {
            for (IndexExpression expression : clause) {
                if (logger.isDebugEnabled()) {
                    ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(expression.column_name);
                    String predicateValue = new String(expression.getValue());
                    String columnName = Utils.getColumnName(cd);
                    if (logger.isDebugEnabled())
                        logger.debug("Index Searcher - Match Other Indexes - predicate value [" + predicateValue + "] column name [" + columnName + "]");
                }
                //first preference to
                if (index.indexes(expression.column_name)) {
                    if (index instanceof PerRowIndex) {
                        matches.add(expression);
                        searchers.add((IndexSearcher) ((PerRowIndex) index).createSecondaryIndexSearcher(Collections.singleton(expression.column_name)));
                    } else if (index instanceof PerColIndex) {
                        matches.add(expression);
                        searchers.add((IndexSearcher) ((PerColIndex) index).createSecondaryIndexSearcher(Collections.singleton(expression.column_name)));
                    }
                    //else continue
                }
            }
        }
        return Pair.create(searchers, matches);
    }

    protected FilterChain getFilterChain(int maxResults, List<IndexExpression> clone) throws QueryNodeException, ReadTimeoutException {
        if (clone.isEmpty())
            return null;
        Pair<List<IndexSearcher>, List<IndexExpression>> searchersAndMatches = matchOtherIndexes(clone);
        List<IndexSearcher> searchers = searchersAndMatches.left;
        List<IndexExpression> matches = searchersAndMatches.right;
        clone.removeAll(matches);
        if (searchers == null || searchers.isEmpty())
            return null;
        return searchOtherClauses(maxResults, searchers, matches);
    }


    private FilterChain searchOtherClauses(int maxResults, List<IndexSearcher> searchers, List<IndexExpression> matches) throws QueryNodeException, ReadTimeoutException {
        FilterChain chain = new FilterChain(searchers.size());
        int i = 0;
        for (IndexSearcher searcher : searchers) {
            IndexExpression match = matches.get(i);
            i++;
            final Query query = getQuery(match);
            if (logger.isDebugEnabled()) {
                logger.debug("Search other clauses query-" + query);
            }
            if (query != null)
                chain.add(searcher.searchReturnFilter(maxResults, query), true);
        }
        return chain;
    }


    protected abstract boolean checkIfNotLatestAndRemove(NumericDocValues tsValues, int docId, org.apache.lucene.search.IndexSearcher searcher, ByteBuffer key, ColumnFamily cf) throws IOException;

}
