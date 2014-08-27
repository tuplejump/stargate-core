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

import com.tuplejump.stargate.Utils;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * User: satya
 * An indexer which uses an underlying lucene ControlledRealTimeReopenThread Manager
 */
public class NearRealTimeIndexer implements Indexer {
    private static final Logger logger = LoggerFactory.getLogger(NearRealTimeIndexer.class);

    public static IndexWriterConfig.OpenMode OPEN_MODE = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;

    protected TrackingIndexWriter indexWriter;

    protected ReferenceManager<IndexSearcher> indexSearcherReferenceManager;

    protected NRTCachingDirectory directory;

    protected File file;

    protected Analyzer analyzer;

    protected String indexName;

    protected String keyspaceName;

    protected String cfName;

    protected volatile long latest;

    protected ControlledRealTimeReopenThread<IndexSearcher> reopenThread;

    protected String vNodeName;

    public NearRealTimeIndexer(Analyzer analyzer, String keyspaceName, String cfName, String indexName, String vNodeName) {
        try {
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
        IndexWriter delegate = getIndexWriter(Properties.luceneVersion);
        indexWriter = new TrackingIndexWriter(delegate);
        indexSearcherReferenceManager = new SearcherManager(delegate, true, null);
        reopenThread = new ControlledRealTimeReopenThread<>(indexWriter, indexSearcherReferenceManager, 1, 0.01);
        startReopenThread();
    }


    private IndexWriter getIndexWriter(Version luceneV) throws IOException {
        file = Utils.getDirectory(keyspaceName, cfName, indexName, vNodeName);
        IndexWriterConfig config = new IndexWriterConfig(luceneV, analyzer);
        config.setRAMBufferSizeMB(256);
        config.setOpenMode(OPEN_MODE);
        directory = new NRTCachingDirectory(FSDirectory.open(file), 100, 100);
        logger.warn(indexName + " SG Index - Opened dir[" + file.getAbsolutePath() + "] - Openmode[" + OPEN_MODE + "]");
        return new IndexWriter(directory, config);
    }

    private void startReopenThread() {
        reopenThread.setName(String.format("SGIndex - %s - NRT Reopen Thread", indexName));
        reopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
        reopenThread.setDaemon(true);
        reopenThread.start();
        logger.warn(indexName + " NRT reopen thread  started - " + reopenThread.getName());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    logger.warn(indexName + " NRT Shutdown hook called- Commiting and closing index");
                    close();
                } catch (Exception e) {
                    //do nothing
                }
            }
        });
    }

    @Override
    public void upsert(Iterable<Field> doc, Term idTerm) {
        if (logger.isDebugEnabled())
            logger.debug(indexName + " Indexing fields" + doc);

        try {
            latest = indexWriter.updateDocument(idTerm, doc);
            indexSearcherReferenceManager.maybeRefresh();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(Iterable<Field> doc) {
        if (logger.isDebugEnabled())
            logger.debug(indexName + " Indexing fields" + doc);

        try {
            latest = indexWriter.addDocument(doc);
            indexSearcherReferenceManager.maybeRefresh();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Term... terms) {
        try {
            BooleanQuery q = new BooleanQuery();
            for (Term t : terms) {
                if (logger.isDebugEnabled())
                    logger.debug(indexName + " Delete term - " + t);
                q.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }
            latest = indexWriter.deleteDocuments(q);
            indexSearcherReferenceManager.maybeRefresh();
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
            indexSearcherReferenceManager.release(searcher);
        } catch (IOException e) {
            logger.error("Unable to release searcher", e);
            //do nothing
        }
    }

    @Override
    public IndexSearcher acquire() {
        try {
            reopenThread.waitForGeneration(latest);
            return indexSearcherReferenceManager.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T search(SearcherCallback<T> searcherCallback) {
        IndexSearcher searcher = null;
        try {
            reopenThread.waitForGeneration(latest);
            searcher = indexSearcherReferenceManager.acquire();
            return searcherCallback.doWithSearcher(searcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                indexSearcherReferenceManager.release(searcher);
            } catch (IOException e) {
                logger.error("Unable to release searcher", e);
                //do nothing
            }
        }
    }


    @Override
    public boolean removeIndex() {
        logger.warn("SG NearRealTimeIndexer - Removing index -" + indexName);
        try {
            closeIndex();
            FileUtils.deleteRecursive(file);
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean truncate(long l) {
        try {
            logger.warn("SG NearRealTimeIndexer - Truncating index -" + indexName);
            indexWriter.deleteAll();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLiveSize() {
        if (indexWriter != null) {
            try {
                return indexWriter.getIndexWriter().ramSizeInBytes();
            } catch (Exception e) {
                //ignore
                return 0;
            }

        } else {
            return 0;
        }
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
        logger.warn("SG NearRealTimeIndexer - Closing index -" + indexName);
        reopenThread.interrupt();
        reopenThread.close();
        indexSearcherReferenceManager.close();
        indexWriter.getIndexWriter().close();
        analyzer.close();
    }

    @Override
    public void commit() {
        try {
            logger.warn("SG NearRealTimeIndexer - Committing index -" + indexName);
            indexWriter.getIndexWriter().commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
