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

import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;

/**
 * User: satya
 */
public class MinMax implements Aggregate {

    protected boolean reverse = false;
    Object currentValue;

    String field;
    String alias;
    boolean isNumber;

    public MinMax(AggregateFactory aggregateFactory, boolean isNumber) {
        this.field = aggregateFactory.getField();
        this.alias = aggregateFactory.getAlias();
        this.isNumber = isNumber;
    }

    public MinMax(AggregateFactory aggregateFactory, boolean isNumber, boolean max) {
        this(aggregateFactory, isNumber);
        this.reverse = max;
    }

    @Override
    public void aggregate(Tuple tuple) {

        if (isNumber) {
            Number colValue = (Number) tuple.getValue(field);
            if (currentValue == null) currentValue = colValue;
            if (reverse) {
                currentValue = AggregateFunction.NumberComparator.compareNumbers((Number) currentValue, colValue) > 0 ? currentValue : colValue;
            } else
                currentValue = AggregateFunction.NumberComparator.compareNumbers((Number) currentValue, colValue) < 0 ? currentValue : colValue;

        } else {
            String colValue = tuple.getValue(field).toString();
            if (currentValue == null) currentValue = colValue;
            if (reverse) {
                currentValue = colValue.compareTo(currentValue.toString()) < 0 ? currentValue : colValue;
            } else
                currentValue = colValue.compareTo(currentValue.toString()) > 0 ? currentValue : colValue;
        }
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(alias);
        generator.writeString(currentValue.toString());
        generator.writeEndObject();
    }
}
