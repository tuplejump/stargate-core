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

import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.query.Search;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;

/**
 * User: satya
 * <p/>
 * A searcher which can be used with a SGIndex
 * Includes base features to make lucene queries etc.
 */
public abstract class SearchSupport extends SecondaryIndexSearcher {

    protected static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);
    RowIndex currentIndex;
    Options options;

    public SearchSupport(SecondaryIndexManager indexManager, RowIndex currentIndex, Set<ByteBuffer> columns, ByteBuffer primaryColName, Options options) {
        super(indexManager, columns);
        this.options = options;
        this.currentIndex = currentIndex;
    }


    protected Pair<Query, org.apache.lucene.search.SortField[]> getQuery(IndexExpression predicate) throws Exception {
        ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(predicate.column_name);
        String predicateValue = cd.getValidator().getString(predicate.bufferForValue());
        String columnName = Utils.getColumnName(cd);
        if (logger.isDebugEnabled())
            logger.debug("Index Searcher - query - predicate value [" + predicateValue + "] column name [" + columnName + "]");
        logger.debug("Column name is {}", columnName);
        Search search = Search.fromJson(predicateValue);
        Query query = search.query(options);
        org.apache.lucene.search.SortField[] sort = search.usesSorting() ? search.sort(options) : null;
        return Pair.create(query, sort);
    }


    protected abstract ColumnFamilyStore.AbstractScanIterator searchResultsIterator(SearchSupport searchSupport, ColumnFamilyStore baseCfs, IndexSearcher searcher, ExtendedFilter filter, Iterator<IndexEntryCollector.IndexEntry> topDocs, boolean needsFiltering) throws IOException;

    public abstract boolean deleteIfNotLatest(DecoratedKey decoratedKey, long ts, String pkString, ColumnFamily cf) throws IOException;

    public abstract boolean deleteRowIfNotLatest(DecoratedKey decoratedKey, ColumnFamily cf);
}
