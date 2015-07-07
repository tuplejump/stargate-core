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

import com.tuplejump.stargate.lucene.query.GroupType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 * User: satya
 */
public class PatternGroup {

    GroupType type;
    Step[] steps;

    @JsonCreator
    public PatternGroup(@JsonProperty("type") GroupType type, @JsonProperty("steps") Step[] steps) {
        this.type = type;
        this.steps = steps;
    }


    public Pattern getPattern(Map<String, NamedCondition> transitionConditions) {
        return getPattern(transitionConditions, this);
    }


    private static Pattern getPattern(Map<String, NamedCondition> transitionConditions, PatternGroup patternGroup) {
        Pattern pattern = new Pattern();
        for (Step step : patternGroup.steps) {
            Pattern next;
            if (step.isPattern()) {
                next = getPattern(transitionConditions, step.patternGroup);

            } else {
                NamedCondition condition = transitionConditions.get(step.ref);
                if (condition == null) throw new IllegalArgumentException("No such condition [" + step.ref + "]");
                next = Pattern.match(condition);
            }
            if (step.repeat) next = next.repeat();
            if (step.optional) next = next.optional();
            next = Pattern.capture(next);
            pattern = patternGroup.type == GroupType.OR ? Pattern.branch(pattern, next) : Pattern.chain(pattern, next);
        }
        return pattern.minimize();
    }


}
