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
import java.nio.ByteBuffer;

/**
 * User: satya
 */
public class MinMax implements Aggregate {

    protected boolean reverse = false;
    Object currentValue;

    AbstractType valueValidator;
    String field;
    String alias;
    boolean isNumber;

    public MinMax(AggregateFactory aggregateFactory, AbstractType valueValidator) {
        this.valueValidator = valueValidator;
        this.field = aggregateFactory.getField();
        this.alias = aggregateFactory.getAlias();
        this.isNumber = Tuple.isNumber(valueValidator.asCQL3Type());
    }

    public MinMax(AggregateFactory aggregateFactory, AbstractType valueValidator, boolean max) {
        this(aggregateFactory, valueValidator);
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
        if (isNumber)
            generator.writeNumber(currentValue.toString());
        else
            generator.writeString(valueValidator.getString((ByteBuffer) currentValue));
        generator.writeEndObject();
    }
}
