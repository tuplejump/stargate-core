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
import org.apache.lucene.search.TopDocs;

import java.nio.ByteBuffer;

/**
 * User: satya
 */
public class SimpleRowScanner extends RowScanner {

    public SimpleRowScanner(SearchSupport searchSupport, ColumnFamilyStore table, IndexSearcher searcher, ExtendedFilter filter, TopDocs topDocs, boolean needsFiltering) throws Exception {
        super(searchSupport, table, searcher, filter, topDocs, needsFiltering);
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
