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
import com.tuplejump.stargate.cassandra.ResultMapper;
import com.tuplejump.stargate.lucene.IndexEntryCollector;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.composites.CellName;
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
    public List<Row> process(final ResultMapper resultMapper, final ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
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


        TreeMultimap<ByteBuffer, IndexEntryCollector.IndexEntry> docs = resultMapper.collector.docsByRowKey(table.metadata.getKeyValidator());
        NavigableSet<ByteBuffer> rowKeys = docs.keySet();
        List<Tuple> allMatches = new ArrayList<>();
        for (ByteBuffer rowKey : rowKeys) {
            final DecoratedKey dk = table.partitioner.decorateKey(rowKey);
            ArrayList<IndexEntryCollector.IndexEntry> entries = new ArrayList<>(docs.get(rowKey));
            final ColumnFamily fullSlice = resultMapper.fetchRangeSlice(entries, dk);
            List<Tuple> tuples = Lists.transform(entries, new com.google.common.base.Function<IndexEntryCollector.IndexEntry, Tuple>() {
                @Override
                public Tuple apply(IndexEntryCollector.IndexEntry input) {
                    CellName cellName = resultMapper.clusteringKey(input.primaryKey);
                    ColumnFamily cf = resultMapper.fetchSingleRow(dk, fullSlice, cellName);
                    Tuple tuple = aggregateFunction.createTuple(options);
                    resultMapper.tableMapper.load(positions, tuple, new Row(dk, cf));
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
        Row row = resultMapper.tableMapper.getRowWithMetaColumn(aggregateFunction.getGroup().toByteBuffer());
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
