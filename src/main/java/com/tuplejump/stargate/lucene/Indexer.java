/*
 * Copyright 2014, Tuplejump Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuplejump.stargate.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

/**
 * User: satya
 * An interface for a stargate indexer.
 */
public interface Indexer {

    public boolean removeIndex();

    public boolean truncate(long l);

    public long liveSize();
    public long size();

    public long approxRowCount();

    public void commit();

    public void close();

    void insert(Iterable<Field> doc);

    void upsert(Term term,Iterable<Field> doc);

    public void delete(Term... idTerm);

    void delete(Query q);

    public Analyzer getAnalyzer();

    void release(IndexSearcher searcher);

    IndexSearcher acquire();

}
