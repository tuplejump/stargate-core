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

    public SimpleRowIndexSupport(Options options, ColumnFamilyStore table) {
        super(options, table);
    }

    @Override
    public void indexRow(Indexer indexer, ByteBuffer rowKey, ColumnFamily cf) {
        AbstractType rkValValidator = table.metadata.getKeyValidator();
        DecoratedKey dk = table.partitioner.decorateKey(rowKey);
        if (cf.isMarkedForDelete()) {
            Term term = Fields.idTerm(rkValValidator.getString(rowKey));
            if (logger.isDebugEnabled()) {
                logger.debug("Column family marked for delete -" + dk);
                logger.debug(String.format("RowIndex delete - Key [%s]", term));
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
            fields.addAll(idFields(dk, rkValValidator.getString(rowKey), rowKey, rkValValidator));
            fields.addAll(tsFields(cf.maxTimestamp()));
            indexer.insert(fields);
        }
    }


    @Override
    public String getActualColumnName(ByteBuffer name) {
        return Utils.getColumnNameStr(name);
    }


}
