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

package com.tuplejump.stargate;

import com.tuplejump.stargate.cassandra.RowIndexSupport;
import com.tuplejump.stargate.cassandra.SearchSupport;
import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.NearRealTimeIndexer;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.SearcherCallback;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.RowPosition;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.service.StorageService;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: satya
 * A per row lucene index.
 * This index requires Options to be passed as a json using sg_options as key in  the CQL Index options
 */
public class RowIndex extends PerRowSecondaryIndex {
    protected static final Logger logger = LoggerFactory.getLogger(RowIndex.class);
    Map<Range<Token>, Indexer> indexers = new HashMap<>();
    protected ColumnDefinition columnDefinition;
    protected String keyspace;
    protected String indexName;
    protected String primaryColumnName;
    protected String tableName;
    protected Options options;
    protected RowIndexSupport rowIndexSupport;
    protected CFDefinition tableDefinition;
    private ReadWriteLock indexLock = new ReentrantReadWriteLock();
    private final Lock readLock = indexLock.readLock();
    private final Lock writeLock = indexLock.writeLock();
    static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public RowIndexSupport getRowIndexSupport() {
        return rowIndexSupport;
    }

    public String getPrimaryColumnName() {
        return primaryColumnName;
    }

    public boolean isMetaColumn() {
        return options.primary.isMetaColumn();
    }

    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf) {
        readLock.lock();
        try {
            rowIndexSupport.indexRow(indexer(baseCfs.partitioner.decorateKey(rowKey)), rowKey, cf);
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public void delete(DecoratedKey key) {
        readLock.lock();
        try {
            AbstractType<?> rkValValidator = baseCfs.metadata.getKeyValidator();
            Term term = Fields.rkTerm(rkValValidator.getString(key.key));
            indexer(key).delete(term);
        } finally {
            readLock.unlock();
        }
    }

    public void delete(DecoratedKey decoratedKey, String pkString, Long ts) {
        readLock.lock();
        try {
            indexer(decoratedKey).delete(Fields.idTerm(pkString), Fields.tsTerm(ts));
        } finally {
            readLock.unlock();
        }
    }

    public <T> T search(ExtendedFilter filter, SearcherCallback<T> searcherCallback) {
        List<IndexReader> indexReaders = new ArrayList<>();
        AbstractBounds<RowPosition> keyRange = filter.dataRange.keyRange();
        Range<Token> filterRange = new Range<>(keyRange.left.getToken(), keyRange.right.getToken());
        boolean isSingleToken = filterRange.left.equals(filterRange.right);
        boolean isFullRange = isSingleToken && baseCfs.partitioner.getMinimumToken().equals(filterRange.left);
        Map<Indexer, IndexSearcher> indexSearchers = new HashMap<>();
        for (Map.Entry<Range<Token>, Indexer> entry : indexers.entrySet()) {
            Range<Token> range = entry.getKey();
            boolean intersects = intersects(filterRange, isSingleToken, isFullRange, range);
            if (intersects) {
                Indexer indexer = entry.getValue();
                IndexSearcher searcher = indexer.acquire();
                indexSearchers.put(indexer, searcher);
                indexReaders.add(searcher.getIndexReader());
            }
        }
        IndexReader[] indexReadersArr = new IndexReader[indexReaders.size()];
        indexReaders.toArray(indexReadersArr);
        MultiReader multiReader = new MultiReader(indexReadersArr, false);
        IndexSearcher allSearcher = new IndexSearcher(multiReader, executorService);
        try {
            return searcherCallback.doWithSearcher(allSearcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                multiReader.close();
            } catch (IOException e) {
                logger.error("Could not close reader", e);
            }
            for (Map.Entry<Indexer, IndexSearcher> entry : indexSearchers.entrySet()) {
                entry.getKey().release(entry.getValue());
            }
        }
    }

    private boolean intersects(Range<Token> filterRange, boolean isSingleToken, boolean isFullRange, Range<Token> range) {
        boolean intersects;
        if (isFullRange) intersects = true;
        else if (isSingleToken) intersects = range.contains(filterRange.left);
        else {
            intersects = range.intersects(filterRange);
        }
        return intersects;
    }

    public Indexer indexer(DecoratedKey decoratedKey) {
        for (Map.Entry<Range<Token>, Indexer> entry : indexers.entrySet()) {
            if (entry.getKey().contains(decoratedKey.getToken())) return entry.getValue();
        }
        throw new IllegalStateException("No VNodeIndexer found for indexing key [" + decoratedKey + "]");
    }

    @Override
    public SecondaryIndexSearcher createSecondaryIndexSearcher(Set<ByteBuffer> columns) {
        readLock.lock();
        try {
            waitForIndexBuilt();
            return new SearchSupport(baseCfs.indexManager, this, columns, this.options);
        } finally {
            readLock.unlock();
        }
    }

    private void waitForIndexBuilt() {
        while (true) {
            //spin busy
            //don't give the searcher out till this happens
            if (isIndexBuilt(columnDefinition.name)) break;
        }
    }


    @Override
    public void init() {
        writeLock.lock();
        try {
            assert baseCfs != null;
            assert columnDefs != null;
            assert columnDefs.size() > 0;
            columnDefinition = columnDefs.iterator().next();
            //null comparator since this is a custom index.
            keyspace = baseCfs.metadata.ksName;
            indexName = columnDefinition.getIndexName();
            tableName = baseCfs.name;
            tableDefinition = baseCfs.metadata.getCfDef();
            primaryColumnName = CFDefinition.definitionType.getString(columnDefinition.name).toLowerCase();
            String optionsJson = columnDefinition.getIndexOptions().get(Constants.INDEX_OPTIONS_JSON);
            this.options = Options.getOptions(primaryColumnName, baseCfs, optionsJson);

            logger.warn("Creating new NRT Indexer for {}", indexName);
            indexers = new HashMap<>();
            Collection<Range<Token>> ranges = StorageService.instance.getLocalRanges(keyspace);
            //Collection<Range<Token>> ranges = Collections.singletonList(new Range<Token>(Murmur3Partitioner.MINIMUM.getToken(), Murmur3Partitioner.MINIMUM.getToken()));
            for (Range<Token> range : ranges) {
                Indexer indexer = new NearRealTimeIndexer(this.options.analyzer, keyspace, baseCfs.name, indexName, range.left.toString());
                indexers.put(range, indexer);
            }
            rowIndexSupport = new RowIndexSupport(options, baseCfs);

        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public void validateOptions() throws ConfigurationException {
        assert columnDefs != null && columnDefs.size() == 1;
    }

    @Override
    public String getIndexName() {
        assert indexName != null;
        return indexName;
    }


    @Override
    public boolean indexes(ByteBuffer name) {
        String toCheck = rowIndexSupport.getActualColumnName(name);
        for (String columnName : this.options.getFields().keySet()) {
            boolean areEqual = toCheck.trim().equalsIgnoreCase(columnName.trim());
            if (logger.isDebugEnabled())
                logger.debug(String.format("Comparing name for index - This column name [%s] - Passed column name [%s] - Equal [%s]", columnName, toCheck, areEqual));
            if (areEqual)
                return true;
        }
        return false;
    }

    @Override
    public void forceBlockingFlush() {
        readLock.lock();
        try {
            if (isIndexBuilt(columnDefinition.name)) {
                for (Indexer indexer : indexers.values()) {
                    indexer.commit();
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long getLiveSize() {
        readLock.lock();
        try {
            long size = 0;
            if (isIndexBuilt(columnDefinition.name)) {
                for (Indexer indexer : indexers.values()) {
                    size = (indexer == null) ? 0 : indexer.getLiveSize();
                }
            }
            return size;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * This is not backed by a CF. Instead it is backed by a lucene index.
     *
     * @return null
     */
    @Override
    public ColumnFamilyStore getIndexCfs() {
        return null;
    }


    @Override
    public void removeIndex(ByteBuffer byteBuffer) {
        logger.warn(indexName + " Got call to REMOVE index.");
        invalidate();
    }

    @Override
    public void reload() {
        logger.warn(indexName + " Got call to RELOAD index.");
        if (indexers == null && columnDefinition.getIndexOptions() != null && !columnDefinition.getIndexOptions().isEmpty()) {
            init();
        }
    }


    @Override
    public void invalidate() {
        writeLock.lock();
        try {
            logger.warn("Removing NRT Indexer for {}", indexName);
            for (Indexer indexer : indexers.values()) {
                if (indexer != null) {
                    indexer.removeIndex();
                }
            }
            indexers = null;
            setIndexRemoved();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void truncateBlocking(long l) {
        readLock.lock();
        try {
            for (Indexer indexer : indexers.values()) {
                if (indexer != null) {
                    indexer.truncate(l);
                    logger.warn(indexName + " Truncated index {}.", indexName);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        return "RowIndex [index=" + indexName + ", keyspace=" + keyspace + ", table=" + tableName + ", column=" + primaryColumnName + "]";
    }
}