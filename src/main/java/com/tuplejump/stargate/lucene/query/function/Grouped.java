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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: satya
 */
public class Grouped {

    Map<String, Object> singleValued;
    Map<String, Collection<Object>> multiValued;

    public Grouped(boolean isMultiValued) {
        if (isMultiValued)
            multiValued = new TreeMap<>();
        else
            singleValued = new TreeMap<>();
    }

    public void singleValue(String group, Object value) {
        this.singleValued.put(group, value);
    }

    public Object singleValue(String group) {
        return this.singleValued.get(group);
    }

    public Collection<Object> values(String group) {
        if (multiValued != null) {
            return multiValued.get(group);
        } else {
            return Collections.emptyList();
        }
    }

    public void values(String group, Collection<Object> values) {
        if (multiValued != null) {
            multiValued.put(group, values);
        }
    }

}
