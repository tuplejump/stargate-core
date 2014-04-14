package com.tuplejump.stargate.luc;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;

/**
 * User: satya
 * An interface for a stargate indexer.
 */
public interface Indexer {

    public boolean removeIndex();

    public boolean truncate(long l);

    public long getLiveSize();

    public void commit();

    public void close();

    public void delete(Term... idTerm);

    public void insert(final Field... docFields);

    public Analyzer getAnalyzer();

    public <T> T search(SearcherCallback<T> searcherCallback);

    void insert(Iterable<Field> doc);
}
