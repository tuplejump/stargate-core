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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: satya
 * An indexer which uses an underlying lucene ControlledRealTimeReopenThread Manager
 */
public class BasicIndexer implements Indexer {
    private static final Logger logger = LoggerFactory.getLogger(BasicIndexer.class);

    public static IndexWriterConfig.OpenMode OPEN_MODE = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;

    protected File file;

    protected Analyzer analyzer;

    protected String indexName;

    protected String keyspaceName;

    protected String cfName;

    protected IndexWriter indexWriter;

    protected Directory directory;

    protected String vNodeName;

    protected SearcherManager searcherManager;

    protected AtomicLong records;

    public BasicIndexer(AtomicLong records, Analyzer analyzer, String keyspaceName, String cfName, String indexName, String vNodeName) {
        try {
            this.records = records;
            init(analyzer, keyspaceName, cfName, indexName, vNodeName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void init(Analyzer analyzer, String keyspaceName, String cfName, String indexName, String vNodeName) throws IOException {
        this.indexName = indexName;
        this.keyspaceName = keyspaceName;
        this.cfName = cfName;
        this.analyzer = analyzer;
        this.vNodeName = vNodeName;
        logger.debug(indexName + " Lucene analyzer -" + analyzer);
        logger.debug(indexName + " Lucene version -" + Properties.luceneVersion);
        indexWriter = getIndexWriter(Properties.luceneVersion);
        searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());
    }


    private IndexWriter getIndexWriter(Version luceneV) throws IOException {
        file = LuceneUtils.getDirectory(keyspaceName, cfName, indexName, vNodeName);
        IndexWriterConfig config = new IndexWriterConfig(luceneV, analyzer);
        //config.setInfoStream(System.out);
        directory = FSDirectory.open(file);
        logger.warn(indexName + " SG Index - Opened dir[" + file.getAbsolutePath() + "] - OpenMode[" + OPEN_MODE + "]");
        return new IndexWriter(directory, config);
    }

    @Override
    public void insert(Iterable<Field> doc) {
        if (logger.isDebugEnabled())
            logger.debug(indexName + " Indexing fields" + doc);

        try {
            records.incrementAndGet();
            indexWriter.addDocument(doc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void upsert(Term term,Iterable<Field> doc) {
        if (logger.isDebugEnabled())
            logger.debug(indexName + "Upsert Indexing fields" + doc);

        try {
            records.incrementAndGet();
            indexWriter.updateDocument(term,doc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void delete(Term... terms) {
        BooleanQuery q = new BooleanQuery();
        for (Term t : terms) {
            if (logger.isDebugEnabled())
                logger.debug(indexName + " Delete term - " + t);
            q.add(new TermQuery(t), BooleanClause.Occur.MUST);
        }
        delete(q);
    }

    @Override
    public void delete(Query q) {
        try {
            indexWriter.deleteDocuments(q);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Analyzer getAnalyzer() {
        return analyzer;
    }

    @Override
    public void release(IndexSearcher searcher) {
        try {
            searcherManager.release(searcher);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IndexSearcher acquire() {
        try {
            searcherManager.maybeRefreshBlocking();
            return searcherManager.acquire();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean removeIndex() {
        logger.warn("SG BasicIndexer - Removing index -" + indexName);
        try {
            closeIndex();
            LuceneUtils.deleteRecursive(file);
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean truncate(long l) {
        try {
            logger.warn("SG BasicIndexer - Truncating index -" + indexName);
            indexWriter.deleteAll();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long liveSize() {
        if (indexWriter != null) {
            try {
                return indexWriter.ramSizeInBytes();
            } catch (Exception e) {
                //ignore
                return 0;
            }

        } else {
            return 0;
        }
    }

    @Override
    public long size() {
        if (indexWriter != null) {
            try {
                return calcTotalFileSize(directory);
            } catch (Exception e) {
                //ignore
                return 0;
            }

        } else {
            return 0;
        }
    }

    @Override
    public long approxRowCount() {
        return records.get();
    }

    @Override
    public void close() {
        try {
            closeIndex();
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void closeIndex() throws IOException {
        indexWriter.close();
        analyzer.close();
    }

    @Override
    public void commit() {
        try {
            logger.warn("SG BasicIndexer - Committing index -" + indexName);
            indexWriter.commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long calcTotalFileSize(Directory directory) throws Exception {
        long totalFileSize = 0L;
        String[] files = directory.listAll();
        if (files == null) return totalFileSize;
        for (String file : files) {
            totalFileSize += directory.fileLength(file);
        }
        return totalFileSize;
    }
}
