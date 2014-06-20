package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.Fields;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.filter.IDiskAtomFilter;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: satya
 */
public abstract class RowScanner extends ColumnFamilyStore.AbstractScanIterator {
    ColumnFamilyStore table;
    org.apache.lucene.search.IndexSearcher searcher;
    ExtendedFilter filter;
    ArrayIterator indexIterator;
    boolean needsFiltering;
    SortedDocValues rowKeyValues;
    NumericDocValues tsValues;
    SearchSupport searchSupport;

    public RowScanner(SearchSupport searchSupport, ColumnFamilyStore table, IndexSearcher searcher, ExtendedFilter filter, TopDocs topDocs, boolean needsFiltering) throws Exception {
        this.searchSupport = searchSupport;
        this.table = table;
        this.searcher = searcher;
        this.filter = filter;
        this.needsFiltering = needsFiltering;
        this.rowKeyValues = Fields.getPKDocValues(searcher);
        this.tsValues = Fields.getTSDocValues(searcher);
        indexIterator = new ArrayIterator(topDocs.scoreDocs);

    }

    @Override
    public boolean needsFiltering() {
        return needsFiltering;
    }

    @Override
    protected Row computeNext() {
        DataRange range = filter.dataRange;
        SliceQueryFilter sliceQueryFilter = (SliceQueryFilter) filter.dataRange.columnFilter(ByteBufferUtil.EMPTY_BYTE_BUFFER);
        while (indexIterator.hasNext()) {
            try {
                ScoreDoc scoreDoc = (ScoreDoc) indexIterator.next();
                ByteBuffer primaryKey = Fields.primaryKey(rowKeyValues, scoreDoc.doc);

                Pair<DecoratedKey, IDiskAtomFilter> keyAndFilter = getFilterAndKey(primaryKey, sliceQueryFilter);
                if (keyAndFilter == null) {
                    continue;
                }

                DecoratedKey dk = keyAndFilter.left;
                if (!range.contains(dk)) {
                    if (SearchSupport.logger.isTraceEnabled()) {
                        SearchSupport.logger.trace("Skipping entry {} outside of assigned scan range", dk.token);
                    }
                    continue;
                }

                if (SearchSupport.logger.isTraceEnabled()) {
                    SearchSupport.logger.trace("Returning index hit for {}", dk);
                }
                long ts = tsValues.get(scoreDoc.doc);
                Row row = getRow(keyAndFilter.right, dk, ts);
                if (row == null) {
                    if (SearchSupport.logger.isTraceEnabled())
                        SearchSupport.logger.trace("Returned Row is null");
                    continue;
                }
                return row;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return endOfData();
    }

    private Row getRow(IDiskAtomFilter dataFilter, DecoratedKey dk, long ts) throws IOException {
        ColumnFamily data = table.getColumnFamily(new QueryFilter(dk, table.name, dataFilter, filter.timestamp));
        if (data == null || searchSupport.deleteIfNotLatest(ts, dk.key, data)) {
            return null;
        }
        return new Row(dk, data);
    }


    protected abstract Pair<DecoratedKey, IDiskAtomFilter> getFilterAndKey(ByteBuffer primaryKey, SliceQueryFilter sliceQueryFilter);

    @Override
    public void close() throws IOException {
        //no op
    }

}
