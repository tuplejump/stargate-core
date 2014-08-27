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

import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.filter.IDiskAtomFilter;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.search.IndexSearcher;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * User: satya
 */
public class SimpleRowScanner extends RowScanner {

    public SimpleRowScanner(SearchSupport searchSupport, ColumnFamilyStore table, IndexSearcher searcher, ExtendedFilter filter, Iterator<IndexEntryCollector.IndexEntry> arrayIterator, boolean needsFiltering) throws Exception {
        super(searchSupport, table, searcher, filter, arrayIterator, needsFiltering);
    }

    @Override
    protected Column getMetaColumn(Column firstColumn, String colName, Float score) {
        return new Column(UTF8Type.instance.decompose(colName), UTF8Type.instance.decompose("{\"score\":" + score.toString() + "}"));
    }

    protected Pair<DecoratedKey, IDiskAtomFilter> getFilterAndKey(ByteBuffer primaryKey, SliceQueryFilter sliceQueryFilter) {
        DecoratedKey dk = table.partitioner.decorateKey(primaryKey);
        IDiskAtomFilter dataFilter = filter.columnFilter(primaryKey);
        return Pair.create(dk, dataFilter);
    }

}
