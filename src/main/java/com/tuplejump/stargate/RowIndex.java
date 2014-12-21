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
import com.tuplejump.stargate.lucene.Constants;
import com.tuplejump.stargate.lucene.LuceneUtils;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.SearcherCallback;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
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
    protected CFDefinition tableDefinition;
    private ReadWriteLock indexLock = new ReentrantReadWriteLock();
    private final Lock readLock = indexLock.readLock();
    private final Lock writeLock = indexLock.writeLock();
    IndexContainer indexContainer;
    boolean nearRealTime = false;
    protected volatile long latest;


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
        latest = Stargate.getInstance().publish(rowKey, cf);
    }

    @Override
    public void delete(DecoratedKey key) {
        readLock.lock();
        try {
            AbstractType<?> rkValValidator = baseCfs.metadata.getKeyValidator();
            Term term = LuceneUtils.rkTerm(rkValValidator.getString(key.key));
            indexContainer.indexer(key).delete(term);
        } finally {
            readLock.unlock();
        }
    }

    public void delete(DecoratedKey decoratedKey, String pkString, Long ts) {
        readLock.lock();
        try {
            indexContainer.indexer(decoratedKey).delete(LuceneUtils.idTerm(pkString), LuceneUtils.tsTerm(ts));
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
            if (isIndexBuilt(columnDefinition.name)) break;
        }
        if (!nearRealTime)
            Stargate.getInstance().catchUp(latest);
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
            this.options = CassandraUtils.getOptions(primaryColumnName, baseCfs, optionsJson);
            this.nearRealTime = options.primary.isNearRealTime();

            logger.warn("Creating new RowIndex for {}", indexName);
            indexContainer = new IndexContainer(options.analyzer, keyspace, tableName, indexName);
            rowIndexSupport = new RowIndexSupport(keyspace, indexContainer, options, baseCfs);
            Stargate.getInstance().register(rowIndexSupport);
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
        for (String columnName : this.options.fields.keySet()) {
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
                indexContainer.commit();
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long getLiveSize() {
        readLock.lock();
        try {
            if (isIndexBuilt(columnDefinition.name)) {
                return indexContainer.size();
            }
            return 0;
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
        if (indexContainer == null && columnDefinition.getIndexOptions() != null && !columnDefinition.getIndexOptions().isEmpty()) {
            init();
        }
    }


    @Override
    public void invalidate() {
        writeLock.lock();
        try {
            logger.warn("Removing All Indexers for {}", indexName);
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