package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.Options;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.luc.Indexer;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.ColumnToCollectionType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 */
public class WideRowIndexSupport implements RowIndexSupport {
    private static final Logger logger = LoggerFactory.getLogger(PerRowIndex.class);
    protected Options options;
    Indexer indexer;
    ColumnFamilyStore table;

    public WideRowIndexSupport(Options options, Indexer indexer, ColumnFamilyStore table) {
        this.options = options;
        this.indexer = indexer;
        this.table = table;
    }

    @Override
    public void indexRow(ByteBuffer rowKey, ColumnFamily cf) {
        Map<ByteBuffer, List<Field>> primaryKeysVsFields = new HashMap<>();
        DecoratedKey dk = table.partitioner.decorateKey(rowKey);
        Map<ByteBuffer, Long> timestamps = new HashMap<>();
        AbstractType rowKeyValidator = table.getComparator();
        Iterator<Column> cols = cf.iterator();
        while (cols.hasNext()) {
            Column column = cols.next();
            addColumn(rowKey, primaryKeysVsFields, timestamps, rowKeyValidator, column);
        }
        addToIndex(cf, dk, primaryKeysVsFields, timestamps, rowKeyValidator);
    }

    private void addToIndex(ColumnFamily cf, DecoratedKey dk, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, Long> timestamps, AbstractType rkValValidator) {
        for (Map.Entry<ByteBuffer, List<Field>> entry : primaryKeysVsFields.entrySet()) {
            ByteBuffer pk = entry.getKey();
            List<Field> fields = entry.getValue();
            if (cf.isMarkedForDelete()) {
                if (logger.isDebugEnabled())
                    logger.debug("Column family marked for delete -" + dk);
                delete(pk);
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("Column family update -" + dk);
                fields.addAll(Utils.idFields(table.name, pk, rkValValidator));
                fields.addAll(Utils.tsFields(timestamps.get(pk), table.name));
                indexer.insert(fields);
            }
        }
    }

    private void addColumn(ByteBuffer rowKey, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, Long> timestamps, AbstractType rowKeyValidator, Column column) {
        ByteBuffer columnName = column.name();
        Pair<CompositeType.Builder, String> primaryKeyAndName = makeCompositePK(table, rowKey, column);
        String name = primaryKeyAndName.right;
        CompositeType.Builder builder = primaryKeyAndName.left;
        ByteBuffer primaryKey = builder.build();
        List<Field> fields = primaryKeysVsFields.get(primaryKey);
        if (fields == null) {
            // new pk found
            if (logger.isDebugEnabled()) {
                logger.debug("New PK found");
                logger.debug(rowKeyValidator.getString(primaryKey));
            }
            fields = new LinkedList<>();
            primaryKeysVsFields.put(primaryKey, fields);
            timestamps.put(primaryKey, 0l);
            //first fields for clustering key columns need to be added.
            addClusteringKeyFields(primaryKey, fields, timestamps, column, builder);
        }

        if (logger.isDebugEnabled())
            logger.debug("Got column name {} from CF", name);
        FieldType fieldType = options.fieldTypes.get(name);
        //if fieldType was not found then the column is not indexed
        if (fieldType != null) {
            ColumnDefinition columnDefinition = table.metadata.getColumnDefinitionFromColumnName(columnName);
            addFields(primaryKey, fields, timestamps, column, columnDefinition, name, fieldType, column.value());
        }
    }

    private void addClusteringKeyFields(ByteBuffer primaryKey, List<Field> fields, Map<ByteBuffer, Long> timestamps, Column column, CompositeType.Builder builder) {
        for (Map.Entry<Integer, Pair<String, ByteBuffer>> entry : options.clusteringKeysIndexed.entrySet()) {
            ByteBuffer value = builder.get(entry.getKey());
            ByteBuffer keyColumn = entry.getValue().right;
            ColumnDefinition columnDefinition = table.metadata.getColumnDefinition(keyColumn);
            String keyColumnName = entry.getValue().left;
            FieldType fieldType = options.fieldTypes.get(keyColumnName);
            addFields(primaryKey, fields, timestamps, column, columnDefinition, keyColumnName, fieldType, value);
        }
    }

    private void addFields(ByteBuffer pk, List<Field> fields, Map<ByteBuffer, Long> timestamps, Column iColumn, ColumnDefinition columnDefinition, String columnName, FieldType fieldType, ByteBuffer value) {
        long existingTS = timestamps.get(pk);
        timestamps.put(pk, Math.max(existingTS, iColumn.maxTimestamp()));
        List<Field> fieldsForField = Utils.fields(columnDefinition, columnName, value, fieldType);
        if (logger.isDebugEnabled())
            logger.debug("Adding fields {} for column name {}", fields, columnName);
        fields.addAll(fieldsForField);
    }

    public Pair<CompositeType.Builder, String> makeCompositePK(ColumnFamilyStore baseCfs, ByteBuffer rowKey, Column column) {
        CFDefinition cfDef = baseCfs.metadata.getCfDef();
        CompositeType baseComparator = (CompositeType) baseCfs.getComparator();
        List<AbstractType<?>> types = baseComparator.types;
        int idx = types.get(types.size() - 1) instanceof ColumnToCollectionType ? types.size() - 2 : types.size() - 1;
        int prefixSize = baseComparator.types.size() - (cfDef.hasCollections ? 2 : 1);
        ByteBuffer[] components = baseComparator.split(column.name());
        String colName = CFDefinition.definitionType.getString(components[idx]);
        CompositeType.Builder builder = new CompositeType.Builder(baseComparator);
        builder.add(rowKey);
        for (int i = 0; i < Math.min(prefixSize, components.length); i++)
            builder.add(components[i]);
        return Pair.create(builder, colName);
    }

    private void delete(ByteBuffer pk) {
        Term term = Fields.idTerm(pk);
        if (logger.isDebugEnabled())
            logger.debug(String.format("SGIndex delete - Key [%s]", term));
        indexer.delete(term);
    }

    public String getActualColumnName(ByteBuffer name, CFDefinition cfDef) {
        ByteBuffer colName = ((CompositeType) table.getComparator()).extractLastComponent(name);
        return Utils.getColumnNameStr(colName);
    }

}
