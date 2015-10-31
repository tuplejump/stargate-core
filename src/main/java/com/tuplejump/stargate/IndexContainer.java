package com.tuplejump.stargate;

import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.SearcherCallback;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;

import java.util.Collection;

public interface IndexContainer {
    public static final String INDEX_RECORDS = "index-num-records";

    void updateIndexers(Collection<Range<Token>> ranges);

    <T> T search(SearcherCallback<T> searcherCallback);

    Indexer indexer(DecoratedKey decoratedKey);

    void commit();

    void close();

    long size();

    long liveSize();

    long rowCount();

    void remove();

    void truncate(long l);

    String indexName();
}
