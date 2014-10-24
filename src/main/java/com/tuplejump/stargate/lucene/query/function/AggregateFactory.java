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

import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.db.marshal.AbstractType;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * User: satya
 */
public class AggregateFactory {
    protected String type;
    private String alias;
    private String field;
    protected boolean distinct;

    public AggregateFactory(@JsonProperty("type") String type, @JsonProperty("alias") String alias, @JsonProperty("field") String field, @JsonProperty("distinct") boolean distinct) {
        this.type = type;
        this.alias = alias;
        this.field = field;
        this.distinct = distinct;
    }

    public Aggregate getAggregate(Options options) {
        if (field == null && distinct)
            throw new UnsupportedOperationException("Distinct cannot be specified when field is null");

        AbstractType valueValidator = AggregateFunction.getFieldValidator(options, field);
        if ("count".equalsIgnoreCase(type)) return new Count(this, valueValidator, distinct);
        else if ("sum".equalsIgnoreCase(type)) return new Sum(this, valueValidator, distinct);
        else if ("min".equalsIgnoreCase(type)) return new MinMax(this, valueValidator);
        else if ("max".equalsIgnoreCase(type)) return new MinMax(this, valueValidator, true);
        else if ("values".equalsIgnoreCase(type)) return new Values(this, valueValidator, distinct);
        else throw new UnsupportedOperationException("Unknown function [" + type + "]");
    }

    public String getField() {
        return field != null ? field.toLowerCase() : null;
    }

    public String getAlias() {
        return alias != null ? alias : type;
    }

}
