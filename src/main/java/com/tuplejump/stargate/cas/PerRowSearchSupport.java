package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.luc.Indexer;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: satya
 * <p/>
 * A searcher which is used for searching on a PerRowIndex
 */
public class PerRowSearchSupport extends SearchSupport {

    protected Set<String> fieldNames;

    public PerRowSearchSupport(SecondaryIndexManager indexManager, SecondaryIndex currentIndex, Indexer indexer, Set<ByteBuffer> columns, Set<String> fieldNames, ByteBuffer primaryColName, Map<String, NumericConfig> numericConfigMap) {
        super(indexManager, currentIndex, indexer, columns, primaryColName, numericConfigMap);
        this.fieldNames = fieldNames;
    }

    @Override
    public List<Row> search(ExtendedFilter mainFilter) {
        try {
            assert currentIndex.isIndexBuilt(primaryColName);
            List<IndexExpression> clause = mainFilter.getClause();
            if (logger.isDebugEnabled())
                logger.debug("All IndexExprs {}", clause);
            List<IndexExpression> predicates = matchThisIndex(clause);
            Query query = getBooleanQuery(predicates);
            //This is mainly to allow data ranges to occur on searches with range and data together.
            ExtendedFilter filter = ExtendedFilter.create(baseCfs, mainFilter.dataRange, null, mainFilter.maxRows(), false, mainFilter.timestamp);
            return getRows(filter, query, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected List<IndexExpression> matchThisIndex(List<IndexExpression> clause) {
        List<IndexExpression> matches = new ArrayList<>();
        for (IndexExpression expression : clause) {
            ColumnDefinition cfDef = baseCfs.metadata.getColumnDefinition(expression.column_name);
            String colName = CFDefinition.definitionType.getString(cfDef.name);
            //we only support Equal - Operators should be a part of the lucene query
            if (fieldNames.contains(colName) && expression.op == IndexOperator.EQ) {
                matches.add(expression);
            }
        }
        return matches;
    }


    protected Query getBooleanQuery(List<IndexExpression> predicates) throws QueryNodeException {
        BooleanQuery finalQuery = new BooleanQuery();
        for (IndexExpression predicate : predicates) {
            Query query = getQuery(predicate);
            logger.debug("Index Searcher - query - " + query);
            //CQL for now supports only AND expressions.
            if (query != null)
                finalQuery.add(query, BooleanClause.Occur.MUST);
        }
        return finalQuery;
    }


    @Override
    public boolean isIndexing(List<IndexExpression> clause) {
        List<IndexExpression> exprs = matchThisIndex(clause);
        return exprs != null && !exprs.isEmpty();
    }

    @Override
    public boolean deleteIfNotLatest(long timestamp, ByteBuffer key, ColumnFamily cf) throws IOException {
        PerRowIndex currIdx = ((PerRowIndex) currentIndex);
        Map<String, Map<String, String>> options = currIdx.options.fieldOptions;
        Column lastColumn = null;
        for (ByteBuffer colKey : cf.getColumnNames()) {
            String name = currIdx.rowIndexSupport.getActualColumnName(colKey, currIdx.tableDefinition);
            Map<String, String> option = options.get(name);
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
