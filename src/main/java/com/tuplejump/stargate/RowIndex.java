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

import com.tuplejump.stargate.cassandra.CassandraUtils;
import com.tuplejump.stargate.cassandra.RowIndexSupport;
import com.tuplejump.stargate.cassandra.SearchSupport;
import com.tuplejump.stargate.cassandra.TableMapper;
import com.tuplejump.stargate.lucene.*;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.Murmur3Partitioner;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.concurrent.OpOrder;
import org.apache.commons.collections.map.LRUMap;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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
    protected ColumnDefinition columnDefinition;
    protected String keyspace;
    protected String indexName;
    protected String primaryColumnName;
    protected String tableName;
    protected Options options;
    protected RowIndexSupport rowIndexSupport;
    protected TableMapper tableMapper;
    protected CFMetaData cfMetaData;
    private ReadWriteLock indexLock = new ReentrantReadWriteLock();
    private final Lock readLock = indexLock.readLock();
    private final Lock writeLock = indexLock.writeLock();
    IndexContainer indexContainer;
    boolean nearRealTime = false;
    protected volatile long latest;

    public Map<String, IndexEntryCollector> collectorMap = Collections.synchronizedMap(new LRUMap(10));

    public TableMapper getTableMapper() {
        return tableMapper;
    }

    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf) {
        latest = Stargate.getInstance().publish(rowKey, cf);
    }

    @Override
    public void delete(DecoratedKey key, OpOrder.Group opGroup) {
        deleteByKey(key);
    }

    public void deleteByKey(DecoratedKey key) {
        readLock.lock();
        try {
            AbstractType<?> rkValValidator = baseCfs.metadata.getKeyValidator();
            Term term = LuceneUtils.rowkeyTerm(rkValValidator.getString(key.getKey()));
            indexContainer.indexer(key).delete(term);
        } finally {
            readLock.unlock();
        }
    }

    public void delete(DecoratedKey decoratedKey, String pkString, Long ts) {
        readLock.lock();
        try {
            indexContainer.indexer(decoratedKey).delete(LuceneUtils.primaryKeyTerm(pkString), LuceneUtils.tsTerm(ts));
        } finally {
            readLock.unlock();
        }
    }

    public <T> T search(SearcherCallback<T> searcherCallback) {
        return indexContainer.search(searcherCallback);
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
            if (isIndexBuilt(columnDefinition.name.bytes)) break;
        }
        if (!nearRealTime)
            Stargate.getInstance().catchUp(latest);
    }


    @Override
    public void init() {
        if (!(StorageService.getPartitioner() instanceof Murmur3Partitioner)) {
            throw new RuntimeException("Stargate currently only supports Mumur3Partitioner");
        }
        writeLock.lock();
        final Boolean isInfoLoggingEnabled = logger.isInfoEnabled();
        try {
            assert baseCfs != null;
            assert columnDefs != null;
            assert columnDefs.size() > 0;
            columnDefinition = columnDefs.iterator().next();
            //null comparator since this is a custom index.
            keyspace = baseCfs.metadata.ksName;
            indexName = columnDefinition.getIndexName();
            tableName = baseCfs.name;
            cfMetaData = baseCfs.metadata;
            primaryColumnName = columnDefinition.name.toString().toLowerCase();
            String optionsJson = columnDefinition.getIndexOptions().get(Constants.INDEX_OPTIONS_JSON);
            this.options = CassandraUtils.getOptions(primaryColumnName, baseCfs, optionsJson);
            this.nearRealTime = options.primary.isNearRealTime();

            if (isInfoLoggingEnabled) {
                logger.info("Creating new RowIndex for {}", indexName);
            }
//            indexContainer = new PerVNodeIndexContainer(options.analyzer, keyspace, tableName, indexName);
            indexContainer = new MonolithIndexContainer(options.analyzer, keyspace, tableName, indexName);
            this.tableMapper = new TableMapper(baseCfs, options.primary.isMetaColumn(), columnDefinition);
            rowIndexSupport = new RowIndexSupport(keyspace, indexContainer, options, tableMapper);
            Stargate.getInstance().register(rowIndexSupport);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (isInfoLoggingEnabled) {
                        logger.info("Closing RowIndex for {}", indexName);
                    }
                    if (indexContainer != null)
                        indexContainer.close();
                }
            });
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
    public boolean indexes(CellName name) {
        String toCheck = name.cql3ColumnName(cfMetaData).toString().trim();
        for (String columnName : this.options.fields.keySet()) {
            boolean areEqual = toCheck.equalsIgnoreCase(columnName.trim());
            if (logger.isDebugEnabled())
                logger.debug(String.format("Comparing name for index - This column name [%s] - Passed column name [%s] - Equal [%s]", columnName, toCheck, areEqual));
            if (areEqual)
                return true;
        }
        return false;
    }

    @Override
    public long estimateResultRows() {
        return indexContainer.rowCount();
    }

    @Override
    public void forceBlockingFlush() {
        readLock.lock();
        try {
            if (isIndexBuilt(columnDefinition.name.bytes)) {
                //flushes writes to the disk
                //also refreshes readers
                indexContainer.commit();
            }
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
        if (logger.isInfoEnabled()) {
            logger.info("Got call to REMOVE index {}.", indexName);
        }
        invalidate();
    }

    @Override
    public void reload() {
        if (logger.isInfoEnabled()) {
            logger.info("Got call to RELOAD index {}.", indexName);
        }
        if (indexContainer == null && columnDefinition.getIndexOptions() != null && !columnDefinition.getIndexOptions().isEmpty()) {
            init();
        }
    }


    @Override
    public void invalidate() {
        writeLock.lock();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Removing All Indexers for {}", indexName);
            }
            indexContainer.remove();
            indexContainer = null;
            setIndexRemoved();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void truncateBlocking(long l) {
        readLock.lock();
        try {
            indexContainer.truncate(l);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        return "RowIndex [index=" + indexName + ", keyspace=" + keyspace + ", table=" + tableName + ", column=" + primaryColumnName + "]";
    }


}
