package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.luc.Indexer;
import com.tuplejump.stargate.luc.SearcherCallback;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.exceptions.ReadTimeoutException;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.utils.IFilter;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.BinaryDocValues;
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
 * Includes base features to make lucene queries etc.
 */
public abstract class SearchSupport extends SecondaryIndexSearcher {

    protected static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);
    Analyzer analyzer;
    Map<String, NumericConfig> numericConfigMap;
    final static EscapeQuerySyntax ESCAPER = new EscapeQuerySyntaxImpl();
    SecondaryIndex currentIndex;
    Indexer indexer;
    boolean isKeyWordCheck;
    ByteBuffer primaryColName;


    public SearchSupport(SecondaryIndexManager indexManager, SecondaryIndex currentIndex, Indexer indexer, Set<ByteBuffer> columns, ByteBuffer primaryColName, Map<String, NumericConfig> numericConfigMap) {
        super(indexManager, columns);
        this.analyzer = indexer.getAnalyzer();
        this.numericConfigMap = numericConfigMap;
        this.currentIndex = currentIndex;
        this.indexer = indexer;
        isKeyWordCheck = indexer.getAnalyzer() instanceof KeywordAnalyzer;
        this.primaryColName = primaryColName;
    }


    protected List<Row> getRows(final ExtendedFilter filter, final Query query, final FilterChain chain, final boolean addlFilter) {
        final SearchSupport searchSupport = this;
        SearcherCallback<List<Row>> sc = new SearcherCallback<List<Row>>() {
            @Override
            public List<Row> doWithSearcher(org.apache.lucene.search.IndexSearcher searcher) {
                try {
                    Utils.SimpleTimer timer = Utils.getStartedTimer(logger);
                    List<Row> results;
                    if (query == null) {
                        results = new ArrayList<>();
                    } else {
                        ColumnFamilyStore.AbstractScanIterator iter = new ScanIterator(searchSupport, baseCfs, searcher, filter, chain, query, addlFilter);
                        results = baseCfs.filter(iter, filter);
                    }
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
        } catch (Exception e) {
            logger.warn("Could not parse lucene query", e);
        }
        return null;
    }


    protected Pair<List<SearchSupport>, List<IndexExpression>> matchOtherIndexes(List<IndexExpression> clause) {
        List<IndexExpression> matches = new ArrayList<>();
        List<SearchSupport> searchers = new ArrayList<>();
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
                if (index.indexes(expression.column_name)) {
                    if (index instanceof PerRowIndex) {
                        matches.add(expression);
                        searchers.add((SearchSupport) ((PerRowIndex) index).createSecondaryIndexSearcher(Collections.singleton(expression.column_name)));
                    }
                }
            }
        }
        return Pair.create(searchers, matches);
    }

    protected FilterChain getFilterChain(int maxResults, List<IndexExpression> clone) throws QueryNodeException, ReadTimeoutException {
        if (clone.isEmpty())
            return null;
        Pair<List<SearchSupport>, List<IndexExpression>> searchersAndMatches = matchOtherIndexes(clone);
        List<SearchSupport> searchers = searchersAndMatches.left;
        List<IndexExpression> matches = searchersAndMatches.right;
        clone.removeAll(matches);
        if (searchers == null || searchers.isEmpty())
            return null;
        return searchOtherClauses(maxResults, searchers, matches);
    }


    private FilterChain searchOtherClauses(int maxResults, List<SearchSupport> searchers, List<IndexExpression> matches) throws QueryNodeException, ReadTimeoutException {
        FilterChain chain = new FilterChain(searchers.size());
        int i = 0;
        for (SearchSupport searcher : searchers) {
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

    public abstract boolean deleteIfNotLatest(long ts, ByteBuffer key, ColumnFamily cf) throws IOException;
}
