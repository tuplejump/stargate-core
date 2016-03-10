package com.tuplejump.stargate.lucene;

import com.tuplejump.stargate.cassandra.TableMapper;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.lucene.index.FilteredTermsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocValuesRewriteMethod;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class ClusteringKeyMultiTermQuery extends MultiTermQuery {

    private final Composite start;
    private final Composite stop;
    private final TableMapper tableMapper;

    public ClusteringKeyMultiTermQuery(Composite start, Composite stop, TableMapper tableMapper) {
        super(LuceneUtils.PK_BYTES);
        this.start = start;
        this.stop = stop;
        this.tableMapper = tableMapper;
        this.setRewriteMethod(new DocValuesRewriteMethod());
    }

    @Override
    protected TermsEnum getTermsEnum(Terms terms, AttributeSource attributeSource) throws IOException {
        return new DataRangeFilteredTermsEnum(terms.iterator());
    }

    @Override
    public String toString(String field) {
        StringBuilder sb = new StringBuilder("[");
        sb.append(this.getClass().getName())
                .append("] field-")
                .append(getField())
                .append(" start-")
                .append(start == null ? null : tableMapper.clusteringCType.getString(start))
                .append(" stop-")
                .append(stop == null ? null : tableMapper.clusteringCType.getString(stop));
        return sb.toString();
    }

    private class DataRangeFilteredTermsEnum extends FilteredTermsEnum {

        DataRangeFilteredTermsEnum(TermsEnum tenum) {
            super(tenum);
            setInitialSeekTerm(new BytesRef());
        }

        @Override
        protected AcceptStatus accept(BytesRef term) {
            CellName ck = tableMapper.makeClusteringKey(TableMapper.fromBytesRef(term));
            if (start != null && !start.isEmpty() && tableMapper.clusteringCType.compare(start, ck) > 0) {
                return AcceptStatus.NO;
            }
            if (stop != null && !stop.isEmpty() && tableMapper.clusteringCType.compare(stop, ck) < 0) {
                return AcceptStatus.NO;
            }
            return AcceptStatus.YES;
        }
    }
}
