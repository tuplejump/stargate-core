package com.tuplejump.stargate.luc;

import com.tuplejump.stargate.Utils;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.tuplejump.stargate.Constants.INDEX_FILE_NAME;
import static com.tuplejump.stargate.Constants.LUCENE_VERSION;

/**
 * User: satya
 * An indexer which uses an underlying lucene ControlledRealTimeReopenThread Manager
 */
public class NRTIndexer implements Indexer {
    private static final Logger logger = LoggerFactory.getLogger(NRTIndexer.class);

    public static IndexWriterConfig.OpenMode OPEN_MODE = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;

    protected TrackingIndexWriter indexWriter;

    protected ReferenceManager<IndexSearcher> indexSearcherReferenceManager;

    protected NRTCachingDirectory directory;

    protected Analyzer analyzer;

    protected String indexName;

    protected String keyspaceName;

    protected String cfName;

    protected volatile long latest;

    protected ControlledRealTimeReopenThread<IndexSearcher> reopenThread;

    public NRTIndexer(Map<String, String> options, String keyspaceName, String cfName, String indexName) {
        try {
            init(options, null, keyspaceName, cfName, indexName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NRTIndexer(Map<String, String> options, Map<String, Map<String, String>> perFieldOptions, String keyspaceName, String cfName, String indexName) {
        try {
            init(options, perFieldOptions, keyspaceName, cfName, indexName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void init(Map<String, String> options, Map<String, Map<String, String>> perFieldOptions, String keyspaceName, String cfName, String indexName) throws IOException {
        this.indexName = indexName;
        this.keyspaceName = keyspaceName;
        this.cfName = cfName;
        String versionStr = options.get(LUCENE_VERSION);
        Version luceneV = Version.parseLeniently(versionStr);
        logger.debug(indexName + " Lucene version -" + luceneV);
        Analyzer defaultAnalyzer = AnalyzerFactory.getAnalyzer(options, luceneV);
        if (perFieldOptions == null) {
            analyzer = defaultAnalyzer;
        } else {
            Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> fieldOptions : perFieldOptions.entrySet()) {
                perFieldAnalyzers.put(fieldOptions.getKey(), AnalyzerFactory.getAnalyzer(fieldOptions.getValue(), luceneV));
            }
            analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
        }
        logger.debug(indexName + " Lucene analyzer -" + analyzer);
        IndexWriter delegate = getIndexWriter(luceneV, options);
        indexWriter = new TrackingIndexWriter(delegate);
        indexSearcherReferenceManager = new SearcherManager(delegate, true, null);
        reopenThread = new ControlledRealTimeReopenThread<>(indexWriter, indexSearcherReferenceManager, 1, 0.01);
        startReopenThread();
    }

    private IndexWriter getIndexWriter(Version luceneV, Map<String, String> options) throws IOException {
        options.put(INDEX_FILE_NAME, indexName);
        File dir = Utils.getDirectory(keyspaceName, cfName, options);
        IndexWriterConfig config = new IndexWriterConfig(luceneV, analyzer);
        config.setRAMBufferSizeMB(256);
        config.setOpenMode(OPEN_MODE);
        directory = new NRTCachingDirectory(FSDirectory.open(dir), 100, 100);
        logger.warn(indexName + " SG Index - Opened dir[" + dir.getAbsolutePath() + "] - Openmode[" + OPEN_MODE + "]");
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
    public void insert(final Field... docFields) {
        if (logger.isDebugEnabled()) logger.debug(indexName + " Indexing fields", Arrays.toString(docFields));
        Iterable<Field> doc = new Iterable<Field>() {
            @Override
            public Iterator<Field> iterator() {
                return new ArrayIterator(docFields);
            }
        };
        insert(doc);

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
        logger.warn("SG NRTIndexer - Removing index -" + indexName);
        close();
        try {
            FSDirectory delegate = (FSDirectory) directory.getDelegate();
            FileUtils.deleteRecursive(delegate.getDirectory());
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean truncate(long l) {
        try {
            logger.warn("SG NRTIndexer - Truncating index -" + indexName);
            indexWriter.deleteAll();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLiveSize() {
        if (indexWriter != null) {
            return indexWriter.getIndexWriter().ramSizeInBytes();
        } else {
            return 0;
        }
    }

    @Override
    public void close() {
        try {
            reopenThread.interrupt();
            reopenThread.close();
            logger.warn("SG NRTIndexer - Closing index -" + indexName);
            commit();
            indexWriter.getIndexWriter().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void commit() {
        try {
            logger.warn("SG NRTIndexer - Commiting index -" + indexName);
            indexWriter.getIndexWriter().commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
