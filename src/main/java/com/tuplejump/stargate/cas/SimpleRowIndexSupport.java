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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: satya
 */
public class SimpleRowIndexSupport implements RowIndexSupport {
    private static final Logger logger = LoggerFactory.getLogger(PerRowIndex.class);
    protected Options options;
    Indexer indexer;
    ColumnFamilyStore table;

    public SimpleRowIndexSupport(Options options, Indexer indexer, ColumnFamilyStore table) {
        this.options = options;
        this.indexer = indexer;
        this.table = table;
    }

    @Override
    public void indexRow(ByteBuffer rowKey, ColumnFamily cf) {
        DecoratedKey dk = table.partitioner.decorateKey(rowKey);
        if (cf.isMarkedForDelete()) {
            Term term = Fields.idTerm(rowKey);
            if (logger.isDebugEnabled()) {
                logger.debug("Column family marked for delete -" + dk);
                logger.debug(String.format("PerRowIndex delete - Key [%s]", term));
            }
            indexer.delete(term);
        } else {
            AbstractType rkValValidator = table.metadata.getKeyValidator();
            Iterator<Column> cols = cf.iterator();
            List<Field> fields = new LinkedList<>();
            while (cols.hasNext()) {
                Column iColumn = cols.next();
                ByteBuffer colName = iColumn.name();
                String name = CFDefinition.definitionType.getString(colName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Got column name {} from CF", name);
                }
                FieldType fieldType = options.fieldTypes.get(name);
                //if fieldType was not found then the column is not indexed
                if (fieldType != null) {
                    ColumnDefinition columnDefinition = table.metadata.getColumnDefinitionFromColumnName(colName);
                    List<Field> fieldsForField = Utils.fields(columnDefinition, name, iColumn.value(), fieldType);
                    fields.addAll(fieldsForField);
                }
            }
            if (logger.isDebugEnabled())
                logger.debug("Column family update -" + dk);
            fields.addAll(Utils.idFields(table.name, rowKey, rkValValidator));
            fields.addAll(Utils.tsFields(cf.maxTimestamp(), table.name));
            indexer.insert(fields);
        }
    }

    @Override
    public String getActualColumnName(ByteBuffer name, CFDefinition cfDef) {
        return Utils.getColumnNameStr(name);
    }


}
