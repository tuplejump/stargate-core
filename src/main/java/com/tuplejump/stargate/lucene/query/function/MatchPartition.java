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

package com.tuplejump.stargate.lucene.query.function;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultimap;
import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.cassandra.CassandraUtils;
import com.tuplejump.stargate.cassandra.CustomColumnFactory;
import com.tuplejump.stargate.cassandra.RowScanner;
import com.tuplejump.stargate.lucene.IndexEntryCollector;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.cql3.Relation;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.BasicOperations;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 */
public class MatchPartition implements Function {

    public static final String MATCH_ID = "$match";
    public static final String PATTERN_ID = "$patternId";

    AggregateFunction aggregateFunction;

    Options options;

    State[] states;

    int maxMatches = 1000;

    long now;


    @JsonCreator
    public MatchPartition(@JsonProperty("aggregate") AggregateHolder aggregateHolder, @JsonProperty("pattern") State[] states) throws Exception {
        now = new Date().getTime();
        this.aggregateFunction = aggregateHolder.getAggregateFunction();
        this.states = states;
    }


    @Override
    public void init(Options options) throws Exception {
        this.options = options;
        aggregateFunction.init(options);
        for (State state : states) {
            state.init(options);
        }
    }

    @Override
    public boolean shouldLimit() {
        return false;
    }

    @Override
    public boolean shouldTryScoring() {
        return false;
    }

    @Override
    public List<Row> process(RowScanner rowScanner, CustomColumnFactory customColumnFactory, final ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        Set<String> automatonFields = new HashSet<>();
        for (int i = 0; i < states.length; i++) {
            automatonFields.add(states[i].getAutomatonField());
        }

        final Map<String, Integer> positions = aggregateFunction.getPositions();
        int position = positions.size();
        for (String field : automatonFields) {
            aggregateFunction.getPositions().put(field, position++);
        }
        aggregateFunction.getPositions().put(MATCH_ID, position++);
        aggregateFunction.getPositions().put(PATTERN_ID, position++);

        final boolean[] allExpressions = new boolean[positions.size()];
        boolean[] afExpressions = aggregateFunction.getSimpleExpressions();
        for (int i = 0; i < afExpressions.length; i++) {
            allExpressions[i] = afExpressions[i];
        }
        //automaton fields are all simple expressions
        for (int i = afExpressions.length - 1; i < allExpressions.length; i++) {
            allExpressions[i] = true;
        }
        aggregateFunction.simpleExpressions = allExpressions;


        TreeMultimap<ByteBuffer, IndexEntryCollector.IndexEntry> docs = rowScanner.getCollector().docsByRowKey(table.getComparator());
        NavigableSet<ByteBuffer> rowKeys = docs.keySet();
        List<Tuple> allMatches = new ArrayList<>();
        for (ByteBuffer rowKey : rowKeys) {
            ArrayList<IndexEntryCollector.IndexEntry> entries = new ArrayList<>(docs.get(rowKey));
            IndexEntryCollector.IndexEntry first = entries.get(0);
            IndexEntryCollector.IndexEntry last = entries.get(entries.size() - 1);
            final DecoratedKey dk = table.partitioner.decorateKey(rowKey);
            CompositeType.Builder firstBuilder = CassandraUtils.rowKeyAndBuilder(table, first.primaryKey).right;
            CompositeType.Builder lastBuilder = CassandraUtils.rowKeyAndBuilder(table, last.primaryKey).right;
            QueryFilter sliceQueryFilter = QueryFilter.getSliceFilter(dk, table.name, firstBuilder.buildForRelation(Relation.Type.GTE), lastBuilder.buildForRelation(Relation.Type.LTE), false, Integer.MAX_VALUE, now);
            final ColumnFamily fullSlice = table.getColumnFamily(sliceQueryFilter);
            List<Tuple> tuples = Lists.transform(entries, new com.google.common.base.Function<IndexEntryCollector.IndexEntry, Tuple>() {
                @Override
                public Tuple apply(IndexEntryCollector.IndexEntry input) {
                    CompositeType.Builder builder = CassandraUtils.rowKeyAndBuilder(table, input.primaryKey).right;
                    QueryFilter queryFilter = QueryFilter.getSliceFilter(dk, table.name, builder.build(), builder.buildAsEndOfRange(), false, Integer.MAX_VALUE, now);
                    ColumnFamily cf = CassandraUtils.filterColumnFamily(table, fullSlice, queryFilter);
                    Tuple tuple = aggregateFunction.createTuple(options);
                    CassandraUtils.load(positions, tuple, new Row(dk, cf), table);
                    return tuple;
                }
            });
            ListIterator<Tuple> iter = tuples.listIterator();
            allMatches.addAll(matchPartition(maxMatches, iter));
        }
        //now that all rows have been matched,lets run the matches through the group
        for (Tuple match : allMatches) {
            aggregateFunction.getGroup().addTuple(match);
        }
        Row row = customColumnFactory.getRowWithMetaColumn(table, currentIndex, aggregateFunction.getGroup().toByteBuffer());
        return Collections.singletonList(row);
    }

    public List<Tuple> matchPartition(int maxWindow, ListIterator<Tuple> timeLine) {
        int patternId = 0;
        List<Tuple> patternMatches = new ArrayList<>();
        while (patternId < maxMatches && timeLine.hasNext()) {
            BitSet patternMatched = new BitSet(states.length);
            for (int matchId = 0; matchId < states.length; matchId++) {
                State state = states[matchId];
                int within = state.nextWithin == null ? maxWindow : state.nextWithin;
                Automaton automaton = state.automaton;
                for (int i = 0; i < within && timeLine.hasNext(); i++) {
                    Tuple tuple = timeLine.next();
                    String value = tuple.getValue(state.getAutomatonField()).toString();
                    boolean accepted = BasicOperations.run(automaton, value);
                    if (accepted) {
                        patternMatched.set(matchId);
                        tuple.setValue(MATCH_ID, state.name);
                        tuple.setValue(PATTERN_ID, patternId);
                        patternMatches.add(tuple);
                        break;
                    }
                }
            }
            patternId++;

        }
        return patternMatches;
    }

}
