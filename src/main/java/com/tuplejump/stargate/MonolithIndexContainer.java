package com.tuplejump.stargate;

import com.tuplejump.stargate.lucene.BasicIndexer;
import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.SearcherCallback;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public class MonolithIndexContainer implements IndexContainer {
    protected static final Logger logger = LoggerFactory.getLogger(RowIndex.class);
    Indexer indexer;
    Analyzer analyzer;
    String keyspace;
    String cf;
    String indexName;


    public MonolithIndexContainer(Analyzer analyzer, String keyspace, String cf, String indexName) {
        this.analyzer = analyzer;
        this.keyspace = keyspace;
        this.cf = cf;
        this.indexName = indexName;
    }

    @Override
    public void updateIndexers(Collection<Range<Token>> ranges) {
        if (indexer == null) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding Monolith indexer");
            }
            String rangeStr = "allVNodes";
            AtomicLong records = Stargate.getInstance().getAtomicLong(INDEX_RECORDS + "-" + indexName + "-" + rangeStr);
            indexer = new BasicIndexer(records, analyzer, keyspace, cf, indexName, rangeStr);
        }
    }

    @Override
    public <T> T search(SearcherCallback<T> searcherCallback) {
        IndexSearcher searcher = indexer.acquire();
        try {
            return searcherCallback.doWithSearcher(searcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            indexer.release(searcher);
        }
    }

    @Override
    public Indexer indexer(DecoratedKey decoratedKey) {
        return indexer;
    }

    @Override
    public void commit() {
        indexer.commit();

    }

    @Override
    public void close() {
        indexer.close();
    }

    @Override
    public long size() {
        return (indexer == null) ? 0 : indexer.size();
    }

    @Override
    public long liveSize() {
        return (indexer == null) ? 0 : indexer.liveSize();
    }

    @Override
    public long rowCount() {
        return (indexer == null) ? 0 : indexer.approxRowCount();
    }

    @Override
    public void remove() {
        indexer.removeIndex();
    }

    @Override
    public void truncate(long l) {
        indexer.truncate(l);
    }


    @Override
    public String indexName() {
        return indexName;
    }

}
