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

import com.tuplejump.stargate.lucene.Properties;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;

/**
 * User: satya
 */
public class Sum implements Aggregate {

    double sum = 0;

    Properties.Type cqlType;
    String field;
    String alias;
    boolean distinct;
    Values values;

    public Sum(AggregateFactory aggregateFactory, Properties.Type type, boolean distinct) {
        this.field = aggregateFactory.getField();
        this.alias = aggregateFactory.getAlias();
        this.cqlType = type;
        this.distinct = distinct;
        if (!type.isNumeric()) {
            throw new UnsupportedOperationException("Sum function is available only on numeric types");
        }
        if (distinct) {
            values = new Values(aggregateFactory, distinct);
        }
    }


    public String getFunction() {
        return "sum";
    }


    @Override
    public void aggregate(Tuple tuple) {
        if (distinct) values.aggregate(tuple);
        else add((Number) tuple.getValue(field));
    }

    private void add(Number obj) {
        if (cqlType == Properties.Type.integer) {
            sum += obj.intValue();
        } else if (cqlType == Properties.Type.bigint) {
            sum += obj.longValue();
        } else if (cqlType == Properties.Type.decimal) {
            sum += obj.floatValue();
        } else if (cqlType == Properties.Type.bigdecimal) {
            sum += obj.doubleValue();
        }
    }


    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(alias);
        if (distinct)
            for (Object value : values.values)
                add((Number) value);
        generator.writeNumber(sum);
        generator.writeEndObject();
    }
}
