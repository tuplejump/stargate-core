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
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl;
import org.apache.lucene.search.Query;
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


    protected List<Row> getRows(final ExtendedFilter filter, final Query query, final boolean addlFilter) {
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
                        ColumnFamilyStore.AbstractScanIterator iter = new ScanIterator(searchSupport, baseCfs, searcher, filter, query, addlFilter);
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


    public abstract boolean deleteIfNotLatest(long ts, ByteBuffer key, ColumnFamily cf) throws IOException;
}
