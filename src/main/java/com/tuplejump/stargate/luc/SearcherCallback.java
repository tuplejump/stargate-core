package com.tuplejump.stargate.luc;

import org.apache.lucene.search.IndexSearcher;

/**
 * Interface for a searcher callback.
 * The callback method will be called with the searcher.
 * User: satya
 */
public interface SearcherCallback<T> {

    public T doWithSearcher(IndexSearcher searcher);
}
