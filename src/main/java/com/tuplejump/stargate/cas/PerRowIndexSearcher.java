package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Fields;
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
import org.apache.lucene.index.NumericDocValues;
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
 * Created with IntelliJ IDEA.
 * User: satya
 * Date: 04/07/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PerRowIndexSearcher extends IndexSearcher {

    protected Set<String> fieldNames;

    public PerRowIndexSearcher(SecondaryIndexManager indexManager, SecondaryIndex currentIndex, Indexer indexer, Set<ByteBuffer> columns, Set<String> fieldNames, ByteBuffer primaryColName, Map<String, NumericConfig> numericConfigMap) {
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
            List<IndexExpression> clone = new ArrayList<>();
            clone.addAll(clause);
            clone.removeAll(predicates);
            Query query = getBooleanQuery(predicates);
            FilterChain chain = getFilterChain(mainFilter.maxRows(), clone);
            if (logger.isDebugEnabled())
                logger.debug("IndexExprs not satisfied by PerRowIndex {}", clone);

            ExtendedFilter filter = ExtendedFilter.create(baseCfs, mainFilter.dataRange, clone, mainFilter.maxRows(), false, mainFilter.timestamp);
            return getRows(filter, query, chain, !clone.isEmpty());
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
    protected boolean checkIfNotLatestAndRemove(NumericDocValues tsValues, int docId, org.apache.lucene.search.IndexSearcher searcher, ByteBuffer key, ColumnFamily cf) throws IOException {
        PerRowIndex currIdx = ((PerRowIndex) currentIndex);
        CFDefinition cfDef = baseCfs.metadata.getCfDef();
        long ts = Fields.timestamp(tsValues, docId);
        Map<String, Map<String, String>> options = currIdx.fieldOptions;
        Column lastColumn = null;
        for (ByteBuffer colKey : cf.getColumnNames()) {
            String name = currIdx.getColumnNameString(colKey, cfDef);
            Map<String, String> option = options.get(name);
            //if fieldType was not found then the column is not indexed
            if (option != null) {
                lastColumn = cf.getColumn(colKey);
            }
        }
        if (lastColumn != null && lastColumn.maxTimestamp() > ts) {
            currIdx.delete(key, ts);
            return true;
        }
        return false;
    }
}
