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


package com.tuplejump.stargate.lucene;

import com.google.common.collect.TreeMultimap;
import com.tuplejump.stargate.cassandra.CassandraUtils;
import com.tuplejump.stargate.cassandra.SearchSupport;
import com.tuplejump.stargate.cassandra.TableMapper;
import com.tuplejump.stargate.lucene.query.Search;
import com.tuplejump.stargate.lucene.query.function.AggregateFunction;
import com.tuplejump.stargate.lucene.query.function.Function;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 * A custom lucene collector to retrieve index entries.
 * An IndexEntry reads from DocValues to construct the row key, primary key and timestamp info.
 */
public class IndexEntryCollector implements Collector {
    public static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);
    public final FieldValueHitQueue<IndexEntry> hitQueue;
    int docBase;
    int totalHits;
    int collectedHits;
    boolean queueFull;
    IndexEntry bottom;
    int numHits;
    SortedDocValues primaryKeys;
    SortedDocValues rowKeys;
    List<String> numericDocValueNamesToFetch;
    List<String> binaryDocValueNamesToFetch;
    Map<String, NumericDocValues> numericDocValuesMap = new HashMap<>();
    Map<String, SortedDocValues> stringDocValues = new HashMap<>();
    Options options;
    List<IndexEntry> indexEntries;
    TreeMultimap<DecoratedKey, IndexEntry> indexEntryTreeMultiMap;
    TableMapper tableMapper;
    public final boolean isSorted;
    boolean canByPassRowFetch;
    FieldDoc after = null;
    Integer afterDoc = null;

    public boolean canByPassRowFetch() {
        return canByPassRowFetch;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public int getCollectedHits() {
        return collectedHits;
    }

    public IndexEntryCollector(FieldDoc afterDoc, boolean reverseClustering, TableMapper tableMapper, Search search, Options options, int maxResults) throws IOException {
        this.after = afterDoc;
        Function function = search.function();
        this.tableMapper = tableMapper;
        this.options = options;
        org.apache.lucene.search.SortField[] sortFields = search.usesSorting() ? search.sort(options) : null;
        if (sortFields == null) {
            hitQueue = FieldValueHitQueue.create(search.primaryKeySort(tableMapper, reverseClustering), maxResults);
            isSorted = false;
            FieldComparator<?>[] comparators = hitQueue.getComparators();
            if (afterDoc != null) {
                logger.warn("Got afterDoc " + afterDoc);
                // Tell all comparators their top value:
                for (int i = 0; i < comparators.length; i++) {
                    @SuppressWarnings("unchecked")
                    FieldComparator<Object> comparator = (FieldComparator<Object>) comparators[i];
                    comparator.setTopValue(afterDoc.fields[i]);
                }
            } else {
                ((FieldComparator<Long>) comparators[0]).setTopValue(CassandraUtils.MINIMUM_TOKEN_VALUE);
            }
        } else {
            hitQueue = FieldValueHitQueue.create(sortFields, maxResults);
            isSorted = true;
        }


        numHits = maxResults;
        numericDocValueNamesToFetch = new ArrayList<>();
        binaryDocValueNamesToFetch = new ArrayList<>();

        if (function instanceof AggregateFunction) {
            AggregateFunction aggregateFunction = (AggregateFunction) function;
            List<String> groupByFields = aggregateFunction.getGroupByFields();
            List<String> aggregateFields = aggregateFunction.getAggregateFields();
            boolean abort = false;
            FieldType[] groupDocValueTypes = null;
            if (groupByFields != null && !abort) {
                groupDocValueTypes = new FieldType[groupByFields.size()];
                for (int i = 0; i < groupByFields.size(); i++) {
                    String field = groupByFields.get(i).toLowerCase();
                    FieldType docValType = getDocValueType(options, field);
                    if (docValType == null) {
                        abort = true;
                        break;
                    }
                    groupDocValueTypes[i] = docValType;
                }
            }
            FieldType[] aggDocValueTypes = new FieldType[aggregateFields.size()];
            if (!abort) {
                for (int i = 0; i < aggregateFields.size(); i++) {
                    String field = aggregateFields.get(i);
                    FieldType docValType = getDocValueType(options, field);
                    if (docValType == null) {
                        abort = true;
                        break;
                    }
                    aggDocValueTypes[i] = docValType;
                }
            }
            canByPassRowFetch = !abort;
            if (canByPassRowFetch) {
                if (groupByFields != null)
                    addToFetch(groupByFields.iterator(), groupDocValueTypes);
                addToFetch(aggregateFields.iterator(), aggDocValueTypes);
            }
        }
    }

    private FieldType getDocValueType(Options options, String field) {
        if (field == null) return null;
        FieldType docValType = options.fieldDocValueTypes.get(field);
        if (docValType == null)
            docValType = options.collectionFieldDocValueTypes.get(Constants.dotSplitter.split(field).iterator().next());
        return docValType;
    }

    private void addToFetch(Iterator<String> groupByFields, FieldType[] groupDocValueTypes) {
        int i = 0;
        while (groupByFields.hasNext()) {
            String field = groupByFields.next();
            FieldType docValType = groupDocValueTypes[i++];
            if (docValType != null) {
                if (docValType.numericType() != null)
                    numericDocValueNamesToFetch.add(field);
                else
                    binaryDocValueNamesToFetch.add(field);
            }
        }
    }

    public List<IndexEntry> docs() {
        if (indexEntries == null) {
            indexEntries = new ArrayList<>();
            IndexEntry entry;
            while ((entry = hitQueue.pop()) != null) {
                indexEntries.add(entry);

            }
            Collections.reverse(indexEntries);
        }
        return indexEntries;
    }

    private final Comparator<DecoratedKey> dkComparator = new Comparator<DecoratedKey>() {
        @Override
        public int compare(DecoratedKey o1, DecoratedKey o2) {
            int cmp = o1.getToken().compareTo(o2.getToken());
            if (cmp != 0) {
                return cmp;
            } else {
                return tableMapper.primaryKeyAbstractType.compare(o1.getKey(), o2.getKey());
            }
        }
    };

    private final Comparator<IndexEntry> entryComparator = new Comparator<IndexEntry>() {
        @Override
        public int compare(IndexEntry o1, IndexEntry o2) {
            return tableMapper.clusteringCType.compare(o1.clusteringKey(), o2.clusteringKey());
        }
    };

    public TreeMultimap<DecoratedKey, IndexEntry> docsByRowKey() {
        if (indexEntries != null) throw new IllegalStateException("Hit queue already traversed");
        if (indexEntryTreeMultiMap == null) {
            indexEntryTreeMultiMap = TreeMultimap.create(dkComparator, entryComparator);
            IndexEntry entry;
            while ((entry = hitQueue.pop()) != null) {
                indexEntryTreeMultiMap.put(entry.decoratedKey(), entry);
            }
        }
        return indexEntryTreeMultiMap;
    }


    private void setIndexEntryValues(int doc, IndexEntry indexEntry) throws IOException {
        ByteBuffer primaryKey = LuceneUtils.byteBufferDocValue(primaryKeys, doc);
        ByteBuffer rowKey = LuceneUtils.byteBufferDocValue(rowKeys, doc);
        Map<String, Number> numericDocValues = null;

        if (!numericDocValueNamesToFetch.isEmpty()) {
            numericDocValues = new HashMap<>();
            for (Map.Entry<String, NumericDocValues> entry : numericDocValuesMap.entrySet()) {
                Type type = AggregateFunction.getLuceneType(options, entry.getKey());
                Number number = LuceneUtils.numericDocValue(entry.getValue(), doc, type);
                numericDocValues.put(entry.getKey(), number);
            }
        }
        Map<String, String> binaryDocValues = null;
        if (!binaryDocValueNamesToFetch.isEmpty()) {
            binaryDocValues = new HashMap<>();
            for (Map.Entry<String, SortedDocValues> entry : stringDocValues.entrySet()) {
                binaryDocValues.put(entry.getKey(), LuceneUtils.stringDocValue(entry.getValue(), doc));
            }
        }
        indexEntry.setIndexEntryValue(rowKey, primaryKey, numericDocValues, binaryDocValues);
    }


    @Override
    public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
        docBase = context.docBase;
        primaryKeys = LuceneUtils.getPKBytesDocValues(context.reader());
        rowKeys = LuceneUtils.getRKBytesDocValues(context.reader());
        if (after != null) afterDoc = after.doc - docBase;
        for (String docValName : numericDocValueNamesToFetch) {
            numericDocValuesMap.put(docValName, context.reader().getNumericDocValues(docValName));
        }
        for (String docValName : binaryDocValueNamesToFetch) {
            stringDocValues.put(docValName, context.reader().getSortedDocValues(docValName));
        }

        return new LeafCollector() {
            final LeafFieldComparator[] comparators = hitQueue.getComparators(context);
            final int[] reverseMul = hitQueue.getReverseMul();

            Scorer scorer;


            protected final int compareBottom(int doc) throws IOException {
                int cmp = reverseMul[0] * comparators[0].compareBottom(doc);
                if (cmp != 0) {
                    return cmp;
                }
                for (int i = 1; i < comparators.length; ++i) {
                    cmp = reverseMul[i] * comparators[i].compareBottom(doc);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            }

            protected final void copy(int slot, int doc) throws IOException {
                for (LeafFieldComparator comparator : comparators) {
                    comparator.copy(slot, doc);
                }
            }

            protected final void setBottom(int slot) {
                for (LeafFieldComparator comparator : comparators) {
                    comparator.setBottom(slot);
                }
            }

            protected final int compareTop(int doc) throws IOException {
                int cmp = reverseMul[0] * comparators[0].compareTop(doc);
                if (cmp != 0) {
                    return cmp;
                }
                for (int i = 1; i < comparators.length; ++i) {
                    cmp = reverseMul[i] * comparators[i].compareTop(doc);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            }

            final void updateBottom(int doc, float score) throws IOException {
                bottom.doc = docBase + doc;
                bottom.score = score;
                setIndexEntryValues(doc, bottom);
                bottom = hitQueue.updateTop();
            }

            @Override
            public void setScorer(Scorer scorer) throws IOException {
                // set the scorer on all comparators
                this.scorer = scorer;
                for (LeafFieldComparator comparator : comparators) {
                    comparator.setScorer(scorer);
                }

            }


            @Override
            public void collect(int doc) throws IOException {
                //System.out.println("  collect doc=" + doc);

                totalHits++;

                float score = Float.NaN;

                if (queueFull) {
                    // Fastmatch: return if this hit is no better than
                    // the worst hit currently in the queue:
                    final int cmp = compareBottom(doc);
                    if (cmp <= 0) {
                        // not competitive since documents are visited in doc id order
                        return;
                    }
                }

                final int topCmp = compareTop(doc);
                if (topCmp > 0 || (topCmp == 0 && doc <= afterDoc)) {
                    // Already collected on a previous page
                    return;
                }

                if (queueFull) {
                    // This hit is competitive - replace bottom element in queue & adjustTop
                    copy(bottom.slot, doc);

                    updateBottom(doc, score);

                    setBottom(bottom.slot);
                } else {
                    collectedHits++;

                    // Startup transient: queue hasn't gathered numHits yet
                    final int slot = collectedHits - 1;
                    //System.out.println("    slot=" + slot);
                    // Copy hit into queue
                    copy(slot, doc);

                    IndexEntry entry = new IndexEntry(slot, docBase + doc, score);
                    setIndexEntryValues(doc, entry);
                    bottom = hitQueue.add(entry);
                    queueFull = collectedHits == numHits;
                    if (queueFull) {
                        setBottom(bottom.slot);
                    }
                }
            }
        };
    }

    @Override
    public boolean needsScores() {
        return false;
    }


    public class IndexEntry extends FieldValueHitQueue.Entry {

        public ByteBuffer primaryKey;
        public ByteBuffer rowKey;
        public Map<String, Number> numericDocValuesMap;
        public Map<String, String> binaryDocValuesMap;


        public IndexEntry(int slot, int doc, float score) {
            super(slot, doc, score);

        }

        public void setIndexEntryValue(ByteBuffer rowKey, ByteBuffer primaryKey, Map<String, Number> numericDocValuesMap, Map<String, String> binaryDocValuesMap) {
            this.rowKey = rowKey;
            this.primaryKey = primaryKey;
            this.numericDocValuesMap = numericDocValuesMap;
            this.binaryDocValuesMap = binaryDocValuesMap;
        }


        public final DecoratedKey decoratedKey() {
            return tableMapper.decorateKey(rowKey);
        }


        public Number getNumber(String field) {
            return numericDocValuesMap.get(field);
        }

        public String getString(String field) {
            return binaryDocValuesMap.get(field);
        }

        public String pkName() {
            ByteBuffer primaryKeyBuff = tableMapper.primaryKey(rowKey, clusteringKey());
            return tableMapper.primaryKeyType.getString(primaryKeyBuff);
        }

        public CellName clusteringKey() {
            return tableMapper.makeClusteringKey(primaryKey);
        }
    }


}
