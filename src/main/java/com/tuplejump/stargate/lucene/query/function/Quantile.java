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

import com.clearspring.analytics.stream.quantile.TDigest;
import com.tuplejump.stargate.lucene.Properties;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: satya
 */
public class Quantile implements Aggregate {
    String alias;
    TDigest accumulator;
    Properties.Type cqlType;
    String field;

    public Quantile(AggregateFactory aggregateFactory, Properties.Type type) {
        this.field = aggregateFactory.getField();
        this.alias = aggregateFactory.getAlias();
        this.cqlType = type;
        String compressionStr = aggregateFactory.dynamicProperties().get("compression");
        int compression = 100;
        if (compressionStr != null) {
            try {
                compression = Integer.parseInt(compressionStr);
            } catch (Exception e) {
                compression = 100;//do nothing
            }
        }
        accumulator = new TDigest(compression);
    }

    @Override
    public void aggregate(Tuple tuple) {
        add((Number) tuple.getValue(field));
    }

    private void add(Number obj) {
        if (cqlType == Properties.Type.integer) {
            accumulator.add((Integer) obj);
        } else if (cqlType == Properties.Type.bigint) {
            accumulator.add((Long) obj);
        } else if (cqlType == Properties.Type.decimal) {
            accumulator.add((Float) obj);
        } else if (cqlType == Properties.Type.bigdecimal) {
            accumulator.add((Double) obj);
        }
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(alias);
        generator.writeString(javax.xml.bind.DatatypeConverter.printBase64Binary(getBytes(accumulator)));
        generator.writeEndObject();
    }

    private static byte[] getBytes(TDigest digest) throws IOException {
        int l = digest.byteSize();
        ByteBuffer bb = ByteBuffer.allocate(l);
        digest.asSmallBytes(bb);
        bb.flip();
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return b;
    }
}
