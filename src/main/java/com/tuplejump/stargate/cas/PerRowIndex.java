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
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
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
 * A per row lucene index.
 */
public class PerRowIndex extends PerRowSecondaryIndex {
    private static final Logger logger = LoggerFactory.getLogger(PerRowIndex.class);
    Indexer indexer;
    protected ColumnDefinition columnDef;
    protected String ksName;
    protected String idxName;
    protected String cfName;
    protected String colName;
    protected Map<String, Map<String, String>> fieldOptions;
    protected Map<String, NumericConfig> numericConfigMap;
    protected Map<String, FieldType> fieldTypes;
    protected SortedSet<String> stringColumnNames;
    protected Map<String, String> idxOptions;
    private Lock reloadLock = new ReentrantLock();


    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf) {
        DecoratedKey dk = baseCfs.partitioner.decorateKey(rowKey);
        CFDefinition cfDef = baseCfs.metadata.getCfDef();
        Map<ByteBuffer, List<Field>> primaryKeysVsFields = new HashMap<>();
        Map<ByteBuffer, AbstractType> validators = new HashMap<>();
        Map<ByteBuffer, Long> timestamps = new HashMap<>();
        AbstractType rkValValidator;
        if (cfDef.isComposite) {
            rkValValidator = baseCfs.getComparator();
        } else {
            rkValValidator = baseCfs.metadata.getKeyValidator();
        }
        Iterator<Column> cols = cf.iterator();
        while (cols.hasNext()) {
            Column iColumn = cols.next();
            ByteBuffer key = iColumn.name();
            ByteBuffer pk = rowKey;

            String name;
            if (cfDef.isComposite) {
                Pair<ByteBuffer, String> pkAndName = Utils.makeCompositePK(baseCfs, rowKey, iColumn);
                pk = pkAndName.left;
                name = pkAndName.right;
            } else {
                name = CFDefinition.definitionType.getString(key);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Column family is composite - [" + cfDef.isComposite + "] and column name is [" + name + "]");
            }
            List<Field> fields = primaryKeysVsFields.get(pk);
            if (fields == null) {
                // new pk found
                if (logger.isDebugEnabled()) {
                    logger.debug("New PK found");
                }
                fields = new LinkedList<>();
                primaryKeysVsFields.put(pk, fields);
                validators.put(pk, rkValValidator);
                timestamps.put(pk, 0l);
            }

            if (logger.isDebugEnabled())
                logger.debug("Got column name {} from CF", name);
            FieldType fieldType = fieldTypes.get(name);
            //if fieldType was not found then the column is not indexed
            if (fieldType != null) {
                long existingTS = timestamps.get(pk);
                timestamps.put(pk, Math.max(existingTS, iColumn.maxTimestamp()));
                ColumnDefinition columnDefinition = baseCfs.metadata.getColumnDefinitionFromColumnName(key);
                List<Field> fieldsForField = Utils.fields(columnDefinition, iColumn, name, fieldType);
                if (logger.isDebugEnabled())
                    logger.debug("Adding fields {} for column name {}", fields, name);
                fields.addAll(fieldsForField);
            }
        }

        addToIndex(cf, dk, primaryKeysVsFields, validators, timestamps);
    }

    private void addToIndex(ColumnFamily cf, DecoratedKey dk, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, AbstractType> validators, Map<ByteBuffer, Long> timestamps) {
        for (Map.Entry<ByteBuffer, List<Field>> entry : primaryKeysVsFields.entrySet()) {
            ByteBuffer pk = entry.getKey();
            List<Field> fields = entry.getValue();
            if (cf.isMarkedForDelete()) {
                if (logger.isDebugEnabled())
                    logger.debug("Column family marked for delete -" + dk);
                delete(pk, null);
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("Column family update -" + dk);
                fields.addAll(Utils.idFields(cfName, pk, validators.get(pk)));
                fields.addAll(Utils.tsFields(timestamps.get(pk), cfName));
                indexer.insert(fields);
            }
        }
    }

    protected void delete(ByteBuffer pk, Long ts) {
        Term term = Fields.idTerm(pk);
        if (logger.isDebugEnabled())
            logger.debug(String.format("SGIndex delete - Key [%s]", term));
        if (ts == null)
            indexer.delete(term);
        else {
            indexer.delete(term, Fields.tsTerm(ts));
        }

    }

    @Override
    public void delete(DecoratedKey key) {
        delete(key.key, null);
    }


    @Override
    public SecondaryIndexSearcher createSecondaryIndexSearcher(Set<ByteBuffer> columns) {
        while (!isIndexBuilt(columnDef.name)) {
            //spin busy
            //dont give the searcher out till this happens
        }
        return new PerRowIndexSearcher(baseCfs.indexManager, this, indexer, columns, stringColumnNames, columnDef.name, numericConfigMap);
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
        //getForRow all the fields options.
        fieldOptions = Options.getForRow(optionsJson, colName);
        if (logger.isDebugEnabled())
            logger.debug("SGIndex field options -" + fieldOptions);

        stringColumnNames = new TreeSet<>();
        stringColumnNames.addAll(fieldOptions.keySet());
        fieldTypes = new TreeMap<>();
        numericConfigMap = new HashMap<>();
        for (String columnName : stringColumnNames) {
            Map<String, String> options = fieldOptions.get(columnName);
            FieldType fieldType = Utils.fieldType(options, cfName, colName, columnDef.getValidator());
            fieldTypes.put(columnName, fieldType);
            if (fieldType.numericType() != null) {
                numericConfigMap.put(columnName, Utils.numericConfig(options, fieldType));
            }
        }
        idxOptions = fieldOptions.get(colName);
        if (logger.isDebugEnabled()) {
            logger.debug("SGIndex index options -" + idxOptions);
            logger.debug("SGIndex Column names being indexed -" + stringColumnNames);
        }
        lockSwapIndexer(true);
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
        CFDefinition cfDef = baseCfs.metadata.getCfDef();
        String nameStr;
        try {
            nameStr = getColumnNameString(name, cfDef);
        } catch (IllegalArgumentException e) {
            nameStr = CFDefinition.definitionType.getString(name);
        }
        for (String colNameStr : fieldOptions.keySet()) {
            boolean areEqual = nameStr.trim().equalsIgnoreCase(colNameStr.trim());
            if (logger.isDebugEnabled())
                logger.debug(String.format("Comparing name for index - This column name [%s] - Passed column name [%s] - Equal [%s]", colNameStr, nameStr, areEqual));
            if (areEqual)
                return true;
        }
        return false;
    }

    protected String getColumnNameString(ByteBuffer name, CFDefinition cfDef) {
        String nameStr;
        if (cfDef.isComposite) {
            ByteBuffer colName = ((CompositeType) baseCfs.getComparator()).extractLastComponent(name);
            nameStr = CFDefinition.definitionType.getString(colName);
        } else {
            nameStr = CFDefinition.definitionType.getString(name);
        }


        return nameStr;
    }

    @Override
    public void forceBlockingFlush() {
        indexer.commit();
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
        invalidate();
    }

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
                    indexer = new NRTIndexer(idxOptions, fieldOptions, ksName, cfName, idxName);

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
    public void truncateBlocking(long l) {
        indexer.truncate(l);
    }

}
