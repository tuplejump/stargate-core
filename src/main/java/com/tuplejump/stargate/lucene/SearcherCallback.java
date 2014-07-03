package com.tuplejump.stargate.lucene;

import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * Interface for a searcher callback.
 * The callback method will be called with the searcher. This callback is in place so that lucene IndexSearcher resources
 * are cleaned up properly
 * User: satya
 */
public interface SearcherCallback<T> {

    public T doWithSearcher(IndexSearcher searcher) throws IOException;
}
