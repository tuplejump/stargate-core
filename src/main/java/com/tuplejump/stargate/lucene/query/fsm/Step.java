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

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * User: satya
 */
public class Step {

    public final String ref;

    public final Boolean optional;

    public final Boolean repeat;

    public final PatternGroup patternGroup;

    @JsonCreator
    public Step(@JsonProperty("ref") String ref, @JsonProperty("optional") Boolean optional, @JsonProperty("repeat") Boolean repeat, @JsonProperty("pattern") PatternGroup patternGroup) {
        this.ref = ref;
        this.optional = optional;
        this.repeat = repeat;
        this.patternGroup = patternGroup;
        if (ref != null && patternGroup != null) {
            throw new IllegalArgumentException("Cannot have pattern and condition ref at the same time");
        }
    }

    public boolean isPattern() {
        return this.patternGroup != null;
    }


}
