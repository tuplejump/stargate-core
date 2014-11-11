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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: satya
 */
public class Values implements Aggregate {

    AbstractType valueValidator;
    String field;
    String alias;
    boolean isNumber;
    Collection<Object> values;

    public Values(AggregateFactory aggregateFactory, AbstractType valueValidator, boolean distinct) {
        this.valueValidator = valueValidator;
        this.field = aggregateFactory.getField();
        this.alias = aggregateFactory.getAlias();
        this.isNumber = Tuple.isNumber(valueValidator.asCQL3Type());
        if (distinct) values = new HashSet<>();
        else values = new ArrayList<>();
    }

    @Override
    public void aggregate(Tuple tuple) {
        values.add(tuple.getValue(field));
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(alias);
        generator.writeStartArray();
        for (Object value : values) {
            generator.writeString(value == null ? null : value.toString());
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }
}
