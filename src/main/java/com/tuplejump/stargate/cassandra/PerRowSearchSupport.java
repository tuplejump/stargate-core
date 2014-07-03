package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * User: satya
 * <p/>
 * A searcher which is used for searching on a PerRowIndex
 */
public class PerRowSearchSupport extends SearchSupport {

    protected Set<String> fieldNames;

    public PerRowSearchSupport(SecondaryIndexManager indexManager, SecondaryIndex currentIndex, Indexer indexer, Set<ByteBuffer> columns, ByteBuffer primaryColName, Options options) {
        super(indexManager, currentIndex, indexer, columns, primaryColName, options);
        this.fieldNames = options.fieldTypes.keySet();
    }

    @Override
    public List<Row> search(ExtendedFilter mainFilter) {
        try {
            assert currentIndex.isIndexBuilt(primaryColName);
            List<IndexExpression> clause = mainFilter.getClause();
            if (logger.isDebugEnabled())
                logger.debug("All IndexExprs {}", clause);
            Pair<Query, Sort> queryAndSort = getQuery(matchThisIndex(clause));
            //This is mainly to allow data ranges to occur on searches with range and data together.
            ExtendedFilter filter = ExtendedFilter.create(baseCfs, mainFilter.dataRange, null, mainFilter.maxRows(), false, mainFilter.timestamp);
            return getRows(filter, queryAndSort, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected IndexExpression matchThisIndex(List<IndexExpression> clause) {
        for (IndexExpression expression : clause) {
            ColumnDefinition cfDef = baseCfs.metadata.getColumnDefinition(expression.column_name);
            String colName = CFDefinition.definitionType.getString(cfDef.name);
            //we only support Equal - Operators should be a part of the lucene query
            if (fieldNames.contains(colName) && expression.op == IndexOperator.EQ) {
                return expression;
            } else if (colName.equalsIgnoreCase(((PerRowIndex) this.currentIndex).primaryColumnName)) {
                return expression;
            }
        }
        return null;
    }


    @Override
    public boolean isIndexing(List<IndexExpression> clause) {
        IndexExpression expr = matchThisIndex(clause);
        return expr != null;
    }

    @Override
    protected ColumnFamilyStore.AbstractScanIterator searchResultsIterator(SearchSupport searchSupport, ColumnFamilyStore baseCfs, IndexSearcher searcher, ExtendedFilter filter, TopDocs topDocs, boolean needsFiltering) throws IOException {
        return ((PerRowIndex) currentIndex).getScanIterator(searchSupport, baseCfs, searcher, filter, topDocs, needsFiltering);
    }

    @Override
    public boolean deleteIfNotLatest(long timestamp, ByteBuffer key, ColumnFamily cf) throws IOException {
        PerRowIndex currIdx = ((PerRowIndex) currentIndex);
        Column lastColumn = null;
        for (ByteBuffer colKey : cf.getColumnNames()) {
            String name = currIdx.rowIndexSupport.getActualColumnName(colKey, currIdx.tableDefinition);
            Properties option = options.getFields().get(name);
            //if fieldType was not found then the column is not indexed
            if (option != null) {
                lastColumn = cf.getColumn(colKey);
            }
        }
        if (lastColumn != null && lastColumn.maxTimestamp() > timestamp) {
            currIdx.delete(key, timestamp);
            return true;
        }
        return false;
    }
}
