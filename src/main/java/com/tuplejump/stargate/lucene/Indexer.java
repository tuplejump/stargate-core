package com.tuplejump.stargate.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;

import java.util.List;

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

    void insert(List<Iterable<Field>> docs);

    public void delete(Term... idTerm);

    public void insert(final Field... docFields);

    public Analyzer getAnalyzer();

    public <T> T search(SearcherCallback<T> searcherCallback);

    void insert(Iterable<Field> doc);
}
