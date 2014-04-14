package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.luc.Indexer;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: satya
 */
public class PerColIndexSearcher extends IndexSearcher {

    public PerColIndexSearcher(SecondaryIndexManager indexManager, SecondaryIndex currentIndex, Indexer indexer, Set<ByteBuffer> columns, ByteBuffer primaryColName, Map<String, NumericConfig> numericConfigMap) {
        super(indexManager, currentIndex, indexer, columns, primaryColName, numericConfigMap);
    }


    @Override
    public List<Row> search(ExtendedFilter mainFilter) {
        try {
            assert currentIndex.isIndexBuilt(primaryColName);
            IndexExpression predicates = matchThisIndex(mainFilter.getClause());
            List<IndexExpression> clone = new ArrayList<>();
            clone.addAll(mainFilter.getClause());
            clone.remove(predicates);
            if (logger.isDebugEnabled()) {
                logger.debug("Remaining predicates -{}", clone.toString());
            }

            FilterChain chain = getFilterChain(mainFilter.maxRows(), clone);
            ExtendedFilter filter = ExtendedFilter.create(baseCfs, mainFilter.dataRange, clone, mainFilter.maxRows(), false, mainFilter.timestamp);
            final Query query = getQuery(predicates);
            if (logger.isDebugEnabled()) {
                if (chain != null) {
                    logger.debug(chain.toString());
                }
                logger.debug("Remaining predicates -{}", clone.toString());
                logger.debug("Lucene Query class is {} and query is {}", query.getClass().getName(), query);
            }

            return getRows(filter, query, chain, !clone.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    protected IndexExpression matchThisIndex(List<IndexExpression> clause) {
        for (IndexExpression expression : clause) {
            SecondaryIndex index = indexManager.getIndexForColumn(expression.column_name);
            //we only support Equal for now- Operators should be a part of the lucene query
            if (index != null && expression.op == IndexOperator.EQ && index == currentIndex) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public boolean isIndexing(List<IndexExpression> clause) {
        return matchThisIndex(clause) != null;
    }

    @Override
    protected boolean checkIfNotLatestAndRemove(NumericDocValues tsValues, int docId, org.apache.lucene.search.IndexSearcher searcher, ByteBuffer key, ColumnFamily cf) throws IOException {
        long ts = Fields.timestamp(tsValues, docId);
        if (cf.maxTimestamp() > ts) {
            ((PerColIndex) currentIndex).delete(key, ts);
            return true;
        }
        return false;
    }


}
