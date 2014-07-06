package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.SearcherCallback;
import com.tuplejump.stargate.lucene.query.Search;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: satya
 * <p/>
 * A searcher which can be used with a SGIndex
 * Includes base features to make lucene queries etc.
 */
public abstract class SearchSupport extends SecondaryIndexSearcher {

    protected static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);
    Analyzer analyzer;
    RowIndex currentIndex;
    Indexer indexer;
    boolean isKeyWordCheck;
    ByteBuffer primaryColName;
    Options options;

    public SearchSupport(SecondaryIndexManager indexManager, RowIndex currentIndex, Indexer indexer, Set<ByteBuffer> columns, ByteBuffer primaryColName, Options options) {
        super(indexManager, columns);
        this.analyzer = indexer.getAnalyzer();
        this.options = options;
        this.currentIndex = currentIndex;
        this.indexer = indexer;
        isKeyWordCheck = indexer.getAnalyzer() instanceof KeywordAnalyzer;
        this.primaryColName = primaryColName;
    }


    protected List<Row> getRows(final ExtendedFilter filter, final Pair<Query, Sort> query, final boolean needsFiltering) {
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
                    TopDocs topDocs = searcher.searchAfter(null, query.left, null, maxResults, query.right, true, false);
                    timer2.endLogTime("For TopDocs search for -" + topDocs.totalHits + " results");
                    if (SearchSupport.logger.isDebugEnabled()) {
                        SearchSupport.logger.debug(String.format("Search results [%s]", topDocs.totalHits));
                    }
                    ColumnFamilyStore.AbstractScanIterator iter = searchResultsIterator(searchSupport, baseCfs, searcher, filter, topDocs, needsFiltering);
                    //takes care of paging.
                    results = baseCfs.filter(iter, filter);
                }
                timer.endLogTime("SGIndex Search with results [" + results.size() + "]over all took -");
                return results;

            }
        };
        return indexer.search(sc);
    }

    protected Pair<Query, Sort> getQuery(IndexExpression predicate) throws Exception {
        ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(predicate.column_name);
        String predicateValue = cd.getValidator().getString(predicate.bufferForValue());
        String columnName = Utils.getColumnName(cd);
        if (logger.isDebugEnabled())
            logger.debug("Index Searcher - query - predicate value [" + predicateValue + "] column name [" + columnName + "]");
        logger.debug("Column name is {}", columnName);
        Search search = Search.fromJson(predicateValue);
        Query query = search.query(options);
        Sort sort = search.usesSorting() ? search.sort(options) : null;
        sort = sort == null ? Sort.RELEVANCE : sort;
        return Pair.create(query, sort);
    }


    protected abstract ColumnFamilyStore.AbstractScanIterator searchResultsIterator(SearchSupport searchSupport, ColumnFamilyStore baseCfs, IndexSearcher searcher, ExtendedFilter filter, TopDocs topDocs, boolean needsFiltering) throws IOException;

    public abstract boolean deleteIfNotLatest(long ts, String pkString, ColumnFamily cf) throws IOException;
}
