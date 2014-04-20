package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Constants;
import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.Options;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.luc.Indexer;
import com.tuplejump.stargate.luc.NRTIndexer;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.index.PerColumnSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: satya
 * A per column lucene index.
 */
public class PerColIndex extends PerColumnSecondaryIndex {
    private static final Logger logger = LoggerFactory.getLogger(PerColIndex.class);
    Indexer indexer;
    protected ColumnDefinition columnDef;
    protected String ksName;
    protected String idxName;
    protected String cfName;
    protected String colName;
    protected FieldType fieldType;
    protected Map<String, NumericConfig> numericConfigMap;
    private Map<String, String> idxOptions;
    private Lock reloadLock = new ReentrantLock();

    @Override
    public void delete(ByteBuffer rowKey, IColumn iColumn) {
        long ts = iColumn.maxTimestamp();
        Pair<ByteBuffer, AbstractType> pkAndVal = Utils.getPKAndValidator(rowKey, baseCfs, iColumn);
        Term idTerm = Fields.idTerm(pkAndVal.left);
        Term tsTerm = Fields.tsTerm(ts);
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s SGIndex delete - Key [%s] with timestamp [%s]", idxName, idTerm, tsTerm));
        indexer.delete(idTerm, tsTerm);
    }

    protected void delete(ByteBuffer rowKey, long ts) {
        Term idTerm = Fields.idTerm(rowKey);
        Term tsTerm = Fields.tsTerm(ts);
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s SGIndex delete - Key [%s] with timestamp [%s]", idxName, idTerm, tsTerm));
        indexer.delete(idTerm, tsTerm);
    }

    @Override
    public void insert(ByteBuffer rowKey, IColumn iColumn) {
        List<Field> doc = new LinkedList<>();
        doc.addAll(Utils.tsFields(iColumn.maxTimestamp(), cfName));
        doc.addAll(Utils.idFields(rowKey, baseCfs, cfName, iColumn));
        List<Field> fields = Utils.fields(columnDef, iColumn, colName, fieldType);
        if (logger.isDebugEnabled())
            logger.debug(idxName + " - fields -" + fields.toString());
        doc.addAll(fields);
        indexer.insert(doc);
    }

    @Override
    public void update(ByteBuffer rowKey, IColumn col) {
        insert(rowKey, col);
    }


    @Override
    protected SecondaryIndexSearcher createSecondaryIndexSearcher(Set<ByteBuffer> byteBuffers) {
        while (!isIndexBuilt(columnDef.name)) {
            //spin busy
            //dont give the searcher out till this happens
        }
        return new PerColIndexSearcher(baseCfs.indexManager, this, indexer, byteBuffers, columnDef.name, numericConfigMap);
    }


    @Override
    public void init() {
        assert baseCfs != null && columnDefs != null && columnDefs.size() == 1;
        columnDef = columnDefs.iterator().next();
        //null comparator since this is a custom index.
        ksName = baseCfs.metadata.ksName;
        cfName = baseCfs.metadata.cfName;
        idxName = columnDef.getIndexName();
        colName = CFDefinition.definitionType.getString(columnDef.name);

        String optionsJson = columnDef.getIndexOptions().get(Constants.INDEX_OPTIONS_JSON);
        //getForColumn the idx options from the indexes CF(Table)
        idxOptions = Options.getForColumn(optionsJson, ksName, cfName, colName, idxName);
        fieldType = Utils.fieldType(idxOptions, cfName, colName, columnDef.getValidator());
        numericConfigMap = new HashMap<>();
        if (fieldType.numericType() != null) {
            numericConfigMap.put(colName, Utils.numericConfig(idxOptions, fieldType));
        }

        lockSwapIndexer(true);
        if (logger.isDebugEnabled()) {
            logger.debug(fieldType.toString());
            logger.debug(String.format("Index %s on %s of %s of %s init done.", idxName, colName, cfName, ksName));
        }
    }

    @Override
    public void validateOptions() throws ConfigurationException {
        assert columnDefs != null && columnDefs.size() == 1;
        //This is called before the base CFS is set.
        //So we cannot getForColumn the options from the indexes CF now.
    }

    @Override
    public String getIndexName() {
        assert idxName != null;
        return idxName;
    }


    @Override
    public boolean indexes(ByteBuffer name) {
        String nameStr = CFDefinition.definitionType.getString(name);
        boolean areEqual = nameStr.trim().equalsIgnoreCase(colName.trim());
        if (logger.isTraceEnabled())
            logger.trace(String.format("%s Comparing name for index - This column name [%s] - Passed column name [%s] - Equal [%s]", idxName, colName, nameStr, areEqual));
        return areEqual;
    }

    @Override
    public void forceBlockingFlush() {
        if (isIndexBuilt(columnDef.name)) {
            indexer.commit();
        }
    }

    @Override
    public long getLiveSize() {
        if (indexer != null)
            return indexer.getLiveSize();
        else
            return 0;
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
        logger.warn(idxName + " Got call to REMOVE index.");
        //per column so invalidate this index.
        invalidate();
    }

    /**
     * Re/loads the indexer again
     */
    @Override
    public void reload() {
        logger.warn(idxName + " Got call to RELOAD index.");
        if (isIndexBuilt(columnDef.name)) {
            indexer.commit();
        }
    }

    private void lockSwapIndexer(boolean createAnother) {
        try {
            boolean locked = reloadLock.tryLock();
            if (locked) {
                if (indexer != null) {
                    indexer.removeIndex();
                    indexer = null;
                }
                if (createAnother)
                    indexer = new NRTIndexer(idxOptions, ksName, cfName, idxName);
            } else {
                throw new RuntimeException(String.format("Unable to acquire reload lock for Index %s of %s on %s of %s. Another thread is already reloading it", idxName, colName, cfName, ksName));
            }
        } finally {
            reloadLock.unlock();
        }
    }

    @Override
    public void invalidate() {
        lockSwapIndexer(false);
        setIndexRemoved();
    }

    @Override
    public void truncate(long l) {
        indexer.truncate(l);
    }

}
