package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.Options;
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

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 * Support for indexing rows of Wide tables(tables with clustering columns).
 */
public class WideRowIndexSupport extends RowIndexSupport {

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
            if (cf.isMarkedForDelete() && options.collectionFieldTypes.isEmpty()) {
                if (logger.isDebugEnabled())
                    logger.debug("Column family marked for delete -" + dk);
                delete(pk);
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("Column family update -" + dk);
                fields.addAll(idFields(pk, rkValValidator));
                fields.addAll(tsFields(timestamps.get(pk)));
                indexer.insert(fields);
            }
        }
    }

    private void addColumn(ByteBuffer rowKey, Map<ByteBuffer, List<Field>> primaryKeysVsFields, Map<ByteBuffer, Long> timestamps, AbstractType rowKeyValidator, Column column) {
        ByteBuffer columnNameBuf = column.name();
        Pair<CompositeType.Builder, String> primaryKeyAndName = primaryKeyAndActualColumnName(true, table, rowKey, column);
        String actualColName = primaryKeyAndName.right;
        if (logger.isTraceEnabled())
            logger.trace("Got column name {} from CF", actualColName);
        CompositeType.Builder builder = primaryKeyAndName.left;
        ByteBuffer primaryKey = builder.build();
        List<Field> fields = primaryKeysVsFields.get(primaryKey);
        if (fields == null) {
            // new pk found
            if (logger.isTraceEnabled()) {
                logger.trace("New PK found");
            }
            fields = new LinkedList<>();
            primaryKeysVsFields.put(primaryKey, fields);
            timestamps.put(primaryKey, 0l);
            //first fields for clustering key columns need to be added.
            addClusteringKeyFields(primaryKey, fields, timestamps, column, builder);
        }
        ColumnDefinition columnDefinition = table.metadata.getColumnDefinitionFromColumnName(columnNameBuf);
        if (options.shouldIndex(actualColName)) {
            long existingTS = timestamps.get(primaryKey);
            timestamps.put(primaryKey, Math.max(existingTS, column.maxTimestamp()));
            addFields(column, actualColName, fields, columnDefinition);
        }
    }


    private void addClusteringKeyFields(ByteBuffer primaryKey, List<Field> fields, Map<ByteBuffer, Long> timestamps, Column column, CompositeType.Builder builder) {
        for (Map.Entry<Integer, Pair<String, ByteBuffer>> entry : options.clusteringKeysIndexed.entrySet()) {
            ByteBuffer value = builder.get(entry.getKey());
            ByteBuffer keyColumn = entry.getValue().right;
            ColumnDefinition columnDefinition = table.metadata.getColumnDefinition(keyColumn);
            String keyColumnName = entry.getValue().left;
            FieldType fieldType = options.fieldTypes.get(keyColumnName);
            long existingTS = timestamps.get(primaryKey);
            timestamps.put(primaryKey, Math.max(existingTS, column.maxTimestamp()));
            addField(fields, columnDefinition, keyColumnName, fieldType, value);
        }
    }

    public Pair<CompositeType.Builder, String> primaryKeyAndActualColumnName(boolean withPkBuilder, ColumnFamilyStore baseCfs, ByteBuffer rowKey, Column column) {
        CompositeType baseComparator = (CompositeType) baseCfs.getComparator();
        CFDefinition cfDef = baseCfs.metadata.getCfDef();
        int prefixSize = baseComparator.types.size() - (cfDef.hasCollections ? 2 : 1);
        List<AbstractType<?>> types = baseComparator.types;
        int idx = types.get(types.size() - 1) instanceof ColumnToCollectionType ? types.size() - 2 : types.size() - 1;
        ByteBuffer[] components = baseComparator.split(column.name());
        String colName = CFDefinition.definitionType.getString(components[idx]);
        if (withPkBuilder) {
            CompositeType.Builder builder = pkBuilder(rowKey, baseComparator, prefixSize, components);
            return Pair.create(builder, colName);
        } else {
            return Pair.create(null, colName);
        }
    }

    private CompositeType.Builder pkBuilder(ByteBuffer rowKey, CompositeType baseComparator, int prefixSize, ByteBuffer[] components) {
        CompositeType.Builder builder = new CompositeType.Builder(baseComparator);
        builder.add(rowKey);
        for (int i = 0; i < Math.min(prefixSize, components.length); i++)
            builder.add(components[i]);
        return builder;
    }

    private void delete(ByteBuffer pk) {
        Term term = Fields.idTerm(pk);
        if (logger.isDebugEnabled())
            logger.debug(String.format("SGIndex delete - Key [%s]", term));
        indexer.delete(term);
    }

    public String getActualColumnName(ByteBuffer name) {
        ByteBuffer colName = ((CompositeType) table.getComparator()).extractLastComponent(name);
        return Utils.getColumnNameStr(colName);
    }

}
