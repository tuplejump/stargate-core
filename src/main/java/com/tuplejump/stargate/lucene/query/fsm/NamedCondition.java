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

package com.tuplejump.stargate.lucene.query.fsm;

import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.query.Condition;
import com.tuplejump.stargate.lucene.query.Selector;
import com.tuplejump.stargate.lucene.query.function.MatchPartition;
import com.tuplejump.stargate.lucene.query.function.Tuple;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.Operations;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * User: satya
 */
public class NamedCondition implements Transition<Tuple> {

    Selector caseCondition;

    Automaton automaton;
    String automatonField;

    public final String name;

    @JsonCreator
    public NamedCondition(@JsonProperty("name") String name, @JsonProperty("condition") Condition caseCondition) {
        this.caseCondition = (Selector) caseCondition;
        this.name = name;
    }

    public void init(Options options) throws Exception {
        automatonField = caseCondition.getField();
        automaton = caseCondition.getAutomaton(options);
    }

    public String getAutomatonField() {
        return automatonField != null ? automatonField.toLowerCase() : null;
    }


    @Override
    public boolean matches(Tuple tuple) {
        String value = tuple.getValue(getAutomatonField()).toString();
        return Operations.run(automaton, value);
    }

    @Override
    public double weight() {
        return 1;
    }

    @Override
    public void onMatch(Tuple tuple) {
        tuple.setValue(MatchPartition.MATCH, name);
    }
}
