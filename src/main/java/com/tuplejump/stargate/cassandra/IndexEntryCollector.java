package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.Fields;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldValueHitQueue;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: satya
 * A custom lucene collector to retrieve index entries.
 * An IndexEntry reads from DocValues to construct the row key, primary key and timestamp info.
 */
public class IndexEntryCollector extends Collector {

    FieldValueHitQueue<IndexEntry> hitQueue;
    FieldComparator<?>[] comparators;
    int docBase;
    int totalHits;
    boolean queueFull;
    IndexEntry bottom;
    Scorer scorer;
    int numHits;
    final int[] reverseMul;
    SortedDocValues pkNames;
    SortedDocValues rowKeys;
    NumericDocValues timeStamps;


    public IndexEntryCollector(org.apache.lucene.search.SortField[] sortFields, int maxResults) throws IOException {
        if (sortFields == null) {
            hitQueue = FieldValueHitQueue.create(new org.apache.lucene.search.SortField[]{org.apache.lucene.search.SortField.FIELD_SCORE}, maxResults);
        } else {
            hitQueue = FieldValueHitQueue.create(sortFields, maxResults);
        }
        comparators = hitQueue.getComparators();
        numHits = maxResults;
        reverseMul = hitQueue.getReverseMul();

    }

    public List<IndexEntry> docs() {
        List<IndexEntry> indexEntries = new ArrayList<>();
        IndexEntry entry;
        while ((entry = hitQueue.pop()) != null) {
            indexEntries.add(entry);
        }
        return indexEntries;
    }

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        docBase = context.docBase;
        for (int i = 0; i < comparators.length; i++) {
            hitQueue.setComparator(i, comparators[i].setNextReader(context));
        }
        pkNames = Fields.getPKDocValues(context.reader());
        rowKeys = Fields.getRKDocValues(context.reader());
        timeStamps = Fields.getTSDocValues(context.reader());
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return false;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        // set the scorer on all comparators
        for (int i = 0; i < comparators.length; i++) {
            comparators[i].setScorer(scorer);
        }
        this.scorer = scorer;
    }

    @Override
    public void collect(int doc) throws IOException {
        ++totalHits;
        if (queueFull) {
            // Fastmatch: return if this hit is not competitive
            for (int i = 0; ; i++) {
                final int c = reverseMul[i] * comparators[i].compareBottom(doc);
                if (c < 0) {
                    // Definitely not competitive.
                    return;
                } else if (c > 0) {
                    // Definitely competitive.
                    break;
                } else if (i == comparators.length - 1) {
                    // Here c=0. If we're at the last comparator, this doc is not
                    // competitive, since docs are visited in doc Id order, which means
                    // this doc cannot compete with any other document in the queue.
                    return;
                }
            }

            int slot = bottom.slot;
            // This hit is competitive - replace bottom element in queue & adjustTop
            for (int i = 0; i < comparators.length; i++) {
                comparators[i].copy(slot, doc);
            }

            // Compute score only if it is competitive.
            final float score = scorer.score();
            updateBottom(slot, doc, score);

            for (int i = 0; i < comparators.length; i++) {
                comparators[i].setBottom(bottom.slot);
            }
        } else {
            // Startup transient: queue hasn't gathered numHits yet
            final int slot = totalHits - 1;
            // Copy hit into queue
            for (int i = 0; i < comparators.length; i++) {
                comparators[i].copy(slot, doc);
            }

            // Compute score only if it is competitive.
            final float score = scorer.score();
            add(slot, doc, score);
            if (queueFull) {
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].setBottom(bottom.slot);
                }
            }
        }
    }

    final void updateBottom(int slot, int doc, float score) throws IOException {
        hitQueue.pop();
        bottom = getIndexEntry(slot, doc, score);
        hitQueue.add(bottom);
    }

    final void add(int slot, int doc, float score) throws IOException {
        IndexEntry entry = getIndexEntry(slot, doc, score);
        bottom = hitQueue.add(entry);
        queueFull = (totalHits == numHits);
    }

    IndexEntry getIndexEntry(int slot, int doc, float score) throws IOException {
        String pkName = Fields.primaryKeyName(pkNames, doc);
        ByteBuffer rowKey = Fields.rowKey(rowKeys, doc);
        long timeStamp = timeStamps.get(doc);
        IndexEntry entry = new IndexEntry(pkName, rowKey, timeStamp, slot, docBase + doc, score);
        return entry;
    }


    public static class IndexEntry extends FieldValueHitQueue.Entry {
        public final String pkName;
        public final ByteBuffer rowKey;
        public final long timestamp;
        public float score;


        public IndexEntry(String pkName, ByteBuffer rowKey, long timestamp, int slot, int doc, float score) {
            super(slot, doc, score);
            this.pkName = pkName;
            this.rowKey = rowKey;
            this.timestamp = timestamp;
            this.score = score;
        }

        @Override
        public String toString() {
            return super.toString() + "pkName[" + pkName + "]" + "rowKey[" + rowKey + "]" + "timestamp[" + timestamp + "]";
        }
    }
}
