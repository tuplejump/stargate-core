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

import com.google.common.collect.TreeMultimap;
import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.cassandra.ResultMapper;
import com.tuplejump.stargate.lucene.IndexEntryCollector;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.query.fsm.Matcher;
import com.tuplejump.stargate.lucene.query.fsm.NamedCondition;
import com.tuplejump.stargate.lucene.query.fsm.Pattern;
import com.tuplejump.stargate.lucene.query.fsm.PatternGroup;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.composites.CellName;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

/**
 * User: satya
 */
public class MatchPartition implements Function {

    public static final String MATCH = "$match";
    public static final String MATCH_ID = "$matchId";
    public static final String PATTERN_ID = "$patternId";

    AggregateFunction aggregateFunction;

    Options options;

    NamedCondition[] namedConditions;

    int maxMatches = 1000;

    long now;

    PatternGroup group;

    Pattern pattern;

    @JsonCreator
    public MatchPartition(@JsonProperty("aggregate") AggregateHolder aggregateHolder, @JsonProperty("define") NamedCondition[] namedConditions, @JsonProperty("pattern") PatternGroup group) throws Exception {
        now = new Date().getTime();
        this.aggregateFunction = aggregateHolder.getAggregateFunction();
        this.namedConditions = namedConditions;
        this.group = group;

    }


    @Override
    public void init(Options options) throws Exception {
        this.options = options;
        aggregateFunction.init(options);
        Map<String, NamedCondition> transitionConditions = new HashMap<>();
        for (NamedCondition namedCondition : namedConditions) {
            namedCondition.init(options);
            transitionConditions.put(namedCondition.name, namedCondition);
        }
        this.pattern = group.getPattern(transitionConditions);
    }


    @Override
    public boolean shouldTryScoring() {
        return false;
    }

    @Override
    public List<Row> process(final ResultMapper resultMapper, final ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        Set<String> automatonFields = new HashSet<>();
        for (int i = 0; i < namedConditions.length; i++) {
            automatonFields.add(namedConditions[i].getAutomatonField());
        }

        final Map<String, Integer> positions = aggregateFunction.getPositions();
        int position = positions.size();
        for (String field : automatonFields) {
            aggregateFunction.getPositions().put(field, position++);
        }
        aggregateFunction.getPositions().put(MATCH, position++);
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
        List<Tuple> allMatches = getAllMatches(resultMapper, positions);
        //now that all rows have been matched,lets run the matches through the group
        for (Tuple match : allMatches) {
            if (match != null)
                aggregateFunction.getGroup().addTuple(match);
        }
        Row row = resultMapper.tableMapper.getRowWithMetaColumn(aggregateFunction.getGroup().toByteBuffer());
        return Collections.singletonList(row);
    }

    @Override
    public boolean needsPaging() {
        return false;
    }

    private List<Tuple> getAllMatches(ResultMapper resultMapper, Map<String, Integer> positions) {
        List<Tuple> allMatches = new ArrayList<>();

        TreeMultimap<DecoratedKey, IndexEntryCollector.IndexEntry> docs = resultMapper.docsByRowKey();
        for (final DecoratedKey dk : docs.keySet()) {
            List<IndexEntryCollector.IndexEntry> entries = new ArrayList<>(docs.get(dk));
            final Map<CellName, ColumnFamily> fullSlice = resultMapper.fetchRangeSlice(entries, dk,false);
            List<Tuple> tuples = new ArrayList<>(fullSlice.size());
            for (IndexEntryCollector.IndexEntry entry : entries) {
                CellName cellName = entry.clusteringKey();
                ColumnFamily cf = fullSlice.get(cellName);
                if (cf != null) {
                    Tuple tuple = aggregateFunction.createTuple(options);
                    resultMapper.tableMapper.load(positions, tuple, new Row(dk, cf));
                    tuples.add(tuple);
                }
            }
            int splice = Math.min(tuples.size(), maxMatches);

            allMatches.addAll(matchPartition(tuples.subList(0, splice)));
        }
        return allMatches;
    }

    private List<Tuple> matchPartition(List<Tuple> timeLine) {
        int patternId = 0;
        List<Tuple> patternMatches = new ArrayList<>();
        Matcher matcher = pattern.matcher(timeLine);
        while (matcher.find()) {
            List<Tuple> matchSeq = matcher.group();
            int matchId = 0;
            for (Tuple tuple : matchSeq) {
                tuple.setValue(MATCH_ID, matchId++);
                tuple.setValue(PATTERN_ID, patternId);
            }
            patternMatches.addAll(matchSeq);
            patternId++;
        }
        return patternMatches;
    }

}
