/*
 * Copyright 2014, Stratio.
 * Modification and adapations - Copyright 2014, Tuplejump Inc.
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

package com.tuplejump.stargate.lucene.query;

import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.lucene.Type;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.BasicAutomata;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.text.ParseException;

/**
 * A {@link Condition} implementation that matches documents containing a value for a field.
 */
public class MatchCondition extends Condition implements Selector {

    /**
     * The field name
     */
    private final String field;

    /**
     * The field value
     */
    private Object value;

    /**
     * Constructor using the field name and the value to be matched.
     *
     * @param boost The boost for this query clause. Documents matching this clause will (in addition to the normal
     *              weightings) have their score multiplied by {@code boost}. If {@code null}, then  DEFAULT_BOOST
     *              is used as default.
     * @param field the field name.
     * @param value the field value.
     */
    @JsonCreator
    public MatchCondition(@JsonProperty("boost") Float boost,
                          @JsonProperty("field") String field,
                          @JsonProperty("value") Object value) {
        super(boost);
        this.field = field != null ? field.toLowerCase() : null;
        this.value = value;
    }

    /**
     * Returns the field name.
     *
     * @return the field name.
     */
    public String getField() {
        return field;
    }

    /**
     * Returns the field value.
     *
     * @return the field value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query query(Options schema) throws Exception {

        if (field == null || field.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name required");
        }
        if (value == null || value instanceof String && ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException("Field value required");
        }
        NumericConfig numericConfig = schema.numericFieldOptions.get(field);
        Properties properties = schema.getProperties(field);
        Type fieldType = properties != null ? properties.getType() : Type.text;
        Query query;
        if (fieldType.isCharSeq() || fieldType == Type.bool) {
            String analyzedValue = analyze(field, value.toString(), schema.analyzer);
            if (analyzedValue == null) {
                throw new IllegalArgumentException("Value discarded by analyzer");
            }
            Term term = new Term(field, analyzedValue);
            query = new TermQuery(term);
        } else if (fieldType == Type.integer) {
            assert numericConfig != null;
            Integer value = numericConfig.getNumberFormat().parse(this.value.toString()).intValue();
            query = NumericRangeQuery.newIntRange(field, value, value, true, true);
        } else if (fieldType == Type.bigint || fieldType == Type.date) {
            assert numericConfig != null;
            Long value = numericConfig.getNumberFormat().parse(this.value.toString()).longValue();
            query = NumericRangeQuery.newLongRange(field, value, value, true, true);
        } else if (fieldType == Type.decimal) {
            assert numericConfig != null;
            Float value = numericConfig.getNumberFormat().parse(this.value.toString()).floatValue();
            query = NumericRangeQuery.newFloatRange(field, value, value, true, true);
        } else if (fieldType == Type.bigdecimal) {
            assert numericConfig != null;
            Double value = numericConfig.getNumberFormat().parse(this.value.toString()).doubleValue();
            query = NumericRangeQuery.newDoubleRange(field, value, value, true, true);
        } else {
            String message = String.format("Match queries are not supported by %s field type", fieldType);
            throw new UnsupportedOperationException(message);
        }
        query.setBoost(boost);
        return query;
    }

    @Override
    public String getType() {
        return "match";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(" [boost=");
        builder.append(boost);
        builder.append(", field=");
        builder.append(field);
        builder.append(", value=");
        builder.append(value);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Automaton getAutomaton(Options schema) {
        if (field == null || field.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name required");
        }
        if (value == null || value instanceof String && ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException("Field value required");
        }
        try {
            NumericConfig numericConfig = schema.numericFieldOptions.get(field);
            Properties properties = schema.getProperties(field);
            Type fieldType = properties != null ? properties.getType() : Type.text;
            if (fieldType.isCharSeq()) {
                String analyzedValue = analyze(field, value.toString(), schema.analyzer);
                if (analyzedValue == null) {
                    throw new IllegalArgumentException("Value discarded by analyzer");
                }
                return BasicAutomata.makeString(analyzedValue);
            } else if (fieldType == Type.integer) {
                assert numericConfig != null;
                Integer value = null;

                value = numericConfig.getNumberFormat().parse(this.value.toString()).intValue();

                return BasicAutomata.makeString(value.toString());
            } else if (fieldType == Type.bigint || fieldType == Type.date) {
                assert numericConfig != null;
                Long value = numericConfig.getNumberFormat().parse(this.value.toString()).longValue();
                return BasicAutomata.makeString(value.toString());
            } else if (fieldType == Type.decimal) {
                assert numericConfig != null;
                Float value = numericConfig.getNumberFormat().parse(this.value.toString()).floatValue();
                return BasicAutomata.makeString(value.toString());
            } else if (fieldType == Type.bigdecimal) {
                assert numericConfig != null;
                Double value = numericConfig.getNumberFormat().parse(this.value.toString()).doubleValue();
                return BasicAutomata.makeString(value.toString());
            } else {
                String message = String.format("Match pattern queries are not supported by %s mapper", fieldType);
                throw new UnsupportedOperationException(message);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}