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
import com.tuplejump.stargate.lucene.query.function.Tuple;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.BasicOperations;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * User: satya
 */
public class State {
    Selector condition;
    Automaton automaton;
    String name;

    @JsonCreator
    public State(@JsonProperty("name") String name,
                 @JsonProperty("condition") Condition condition) {
        if (condition instanceof Selector) {
            this.name = name;
            this.condition = (Selector) condition;
        } else {
            throw new UnsupportedOperationException("Selector does not accept condition of type[" + condition.getType() + "]");
        }

    }

    public void init(Options options) {
        automaton = condition.getAutomaton(options);
    }

    public boolean accepts(Tuple tuple) {
        String value = tuple.getValue(condition.getField()).toString();
        return BasicOperations.run(automaton, value);
    }

}
