package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.Options;
import com.tuplejump.stargate.luc.Indexer;
import com.tuplejump.stargate.luc.NRTIndexer;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: satya
 * A per row lucene index.
 */
public class PerRowIndex extends PerRowSecondaryIndex {
    private static final Logger logger = LoggerFactory.getLogger(PerRowIndex.class);
    Indexer indexer;
    protected ColumnDefinition columnDefinition;
    protected String keyspace;
    protected String indexName;
    protected String primaryColumnName;
    protected String tableName;
    protected Options options;
    protected RowIndexSupport rowIndexSupport;
    protected CFDefinition tableDefinition;
    private Lock indexLock = new ReentrantLock();

    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf) {
        rowIndexSupport.indexRow(rowKey, cf);
    }

    @Override
    public void delete(DecoratedKey key) {
        Term term = Fields.idTerm(key.key);
        indexer.delete(term);
    }

    protected void delete(ByteBuffer pk, Long ts) {
        indexer.delete(Fields.idTerm(pk), Fields.tsTerm(ts));
    }


    @Override
    public SecondaryIndexSearcher createSecondaryIndexSearcher(Set<ByteBuffer> columns) {
        while (true) {
            //spin busy
            //don't give the searcher out till this happens
            if (isIndexBuilt(columnDefinition.name)) break;
        }
        return new PerRowSearchSupport(baseCfs.indexManager, this, indexer, columns, options.indexedColumnNames, columnDefinition.name, this.options.numericFieldOptions);
    }


    @Override
    public void init() {
        assert baseCfs != null && columnDefs != null && columnDefs.size() == 1;
        columnDefinition = columnDefs.iterator().next();
        //null comparator since this is a custom index.
        keyspace = baseCfs.metadata.ksName;
        indexName = columnDefinition.getIndexName();
        tableName = baseCfs.name;
        tableDefinition = baseCfs.metadata.getCfDef();
        primaryColumnName = CFDefinition.definitionType.getString(columnDefinition.name).toLowerCase();
        this.options = Options.makeOptions(baseCfs, columnDefinition, primaryColumnName);
        lockSwapIndexer(true);
        if (tableDefinition.isComposite) {
            rowIndexSupport = new WideRowIndexSupport(options, indexer, baseCfs);
        } else {
            rowIndexSupport = new SimpleRowIndexSupport(options, indexer, baseCfs);
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
        CFDefinition cfDef = baseCfs.metadata.getCfDef();
        String toCheck = rowIndexSupport.getActualColumnName(name, cfDef);
        for (String columnName : this.options.fieldOptions.keySet()) {
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
        indexer.commit();
    }

    @Override
    public long getLiveSize() {
        return (indexer == null) ? 0 : indexer.getLiveSize();
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
        if (indexer == null) init();
        if (isIndexBuilt(columnDefinition.name)) {
            indexer.commit();
        }
    }


    private void lockSwapIndexer(boolean createAnother) {
        try {
            boolean locked = indexLock.tryLock();
            if (locked) {
                if (indexer != null) {
                    indexer.removeIndex();
                    indexer = null;
                }
                if (createAnother)
                    indexer = new NRTIndexer(this.options.primaryFieldOptions, this.options.fieldOptions, keyspace, baseCfs.name, indexName);

            } else {
                throw new RuntimeException(String.format("Unable to acquire reload lock for Index %s of %s on %s of %s. Another thread is already reloading it", indexName, primaryColumnName, baseCfs.name, keyspace));
            }
        } finally {
            indexLock.unlock();
        }
    }

    @Override
    public void invalidate() {
        lockSwapIndexer(false);
        setIndexRemoved();
    }

    @Override
    public void truncateBlocking(long l) {
        try {
            boolean locked = indexLock.tryLock();
            if (locked) {
                if (indexer != null) {
                    indexer.truncate(l);
                    logger.warn(indexName + " Truncated index {}.", indexName);
                }

            } else {
                throw new RuntimeException(String.format("Unable to acquire reload lock for Index %s of %s on %s of %s. Another thread is already modifying it", indexName, primaryColumnName, tableName, keyspace));
            }
        } finally {
            indexLock.unlock();
        }
    }

    @Override
    public String toString() {
        return "PerRowIndex [index=" + indexName + ", keyspace=" + keyspace + ", table=" + tableName + ", column=" + primaryColumnName + "]";
    }
}