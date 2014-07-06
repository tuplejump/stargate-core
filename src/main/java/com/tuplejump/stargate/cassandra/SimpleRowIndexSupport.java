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
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: satya
 * Support for indexing simple(without clustering columns) tables
 */
public class SimpleRowIndexSupport extends RowIndexSupport {

    public SimpleRowIndexSupport(Options options, Indexer indexer, ColumnFamilyStore table) {
        super(options, indexer, table);
    }

    @Override
    public void indexRow(ByteBuffer rowKey, ColumnFamily cf) {
        AbstractType rkValValidator = table.metadata.getKeyValidator();
        DecoratedKey dk = table.partitioner.decorateKey(rowKey);
        Term term = Fields.idTerm(rkValValidator.getString(rowKey));
        if (cf.isMarkedForDelete()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Column family marked for delete -" + dk);
                logger.debug(String.format("PerRowIndex delete - Key [%s]", term));
            }
            indexer.delete(term);
        } else {
            Iterator<Column> cols = cf.iterator();
            List<Field> fields = new LinkedList<>();
            while (cols.hasNext()) {
                Column iColumn = cols.next();
                ByteBuffer colName = iColumn.name();
                ColumnDefinition columnDefinition = table.metadata.getColumnDefinitionFromColumnName(colName);
                String name = CFDefinition.definitionType.getString(colName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Got column name {} from CF", name);
                }
                if (options.shouldIndex(name))
                    addFields(iColumn, name, fields, columnDefinition);
            }
            if (logger.isDebugEnabled())
                logger.debug("Column family update -" + dk);
            fields.addAll(idFields(rkValValidator.getString(rowKey), rowKey, rkValValidator));
            fields.addAll(tsFields(cf.maxTimestamp()));
            indexer.upsert(fields, term);
        }
    }


    @Override
    public String getActualColumnName(ByteBuffer name) {
        return Utils.getColumnNameStr(name);
    }


}
