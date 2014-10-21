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

import org.apache.cassandra.db.marshal.AbstractType;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;

/**
 * User: satya
 */
public class Count implements Aggregate {

    long count = 0;
    String alias;
    boolean distinct;
    Values values;

    public Count(AggregateFactory aggregateFactory, AbstractType valueValidator, boolean distinct) {
        this.alias = aggregateFactory.getAlias();
        this.distinct = distinct;
        if (distinct) {
            values = new Values(aggregateFactory, valueValidator, distinct);
        }
    }

    @Override
    public void aggregate(Tuple tuple) {
        if (!distinct) count++;
        else values.aggregate(tuple);
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(alias);
        if (distinct) count = values.values.size();
        generator.writeNumber(count);
        generator.writeEndObject();
    }
}

