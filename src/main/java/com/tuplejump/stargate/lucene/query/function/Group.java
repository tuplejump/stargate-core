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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.db.marshal.AbstractType;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.impl.Utf8Generator;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.util.BufferRecycler;
import org.codehaus.jackson.util.ByteArrayBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * User: satya
 */
public class Group {

    static BufferRecycler bufferRecycler = new BufferRecycler();
    Options options;
    AggregateFactory[] aggregatesToCalculate;
    String[] groupByFields;
    AbstractType[] groupFieldValidators;
    Multimap<Aggregate.Tuple, Aggregate> groups = ArrayListMultimap.create();

    public Group(Options options, AggregateFactory[] aggregatesToCalculate, String[] groupByFields, AbstractType[] groupFieldValidators) {
        this.options = options;
        this.aggregatesToCalculate = aggregatesToCalculate;
        this.groupByFields = groupByFields;
        this.groupFieldValidators = groupFieldValidators;
    }


    public void addTuple(Aggregate.Tuple tuple) {
        Aggregate.Tuple key = tuple.getView(groupByFields);
        Collection<Aggregate> groupValue = groups.get(key);
        if (groupValue.isEmpty()) {
            for (AggregateFactory aggregateFactory : aggregatesToCalculate) {
                Aggregate aggregate = aggregateFactory.getAggregate(options);
                aggregate.aggregate(tuple);
                groups.put(key, aggregate);
            }
        } else {
            for (Aggregate aggregate : groupValue) {
                aggregate.aggregate(tuple);
            }
        }
    }

    public ByteBuffer toByteBuffer() throws IOException {
        ByteArrayBuilder bytes = new ByteArrayBuilder(bufferRecycler);
        IOContext ioContext = new IOContext(bufferRecycler, bytes, false);
        JsonGenerator gen = new Utf8Generator(ioContext, 0, null, bytes);
        gen.writeStartObject();
        gen.writeFieldName("groups");
        gen.writeStartArray();
        for (Aggregate.Tuple tuple : groups.keySet()) {
            gen.writeStartObject();
            gen.writeFieldName("group");
            tuple.writeJson(gen);
            gen.writeFieldName("aggregations");
            gen.writeStartArray();
            Collection<Aggregate> aggregates = groups.get(tuple);
            for (Aggregate aggregate : aggregates) {
                aggregate.writeJson(gen);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
        bytes.flush();
        bytes.close();
        return ByteBuffer.wrap(bytes.toByteArray());
    }


}
