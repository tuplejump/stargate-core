/*
 * Copyright 2014, Stratio.
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
package com.tuplejump.stargate.parse;

import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A {@link Condition} implementation that matches documents containing a value for a field.
 */
public class MatchCondition extends Condition {

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
        if (properties != null) {
            Properties.Type fieldType = properties.getType();
            Query query;
            if (fieldType.isCharSeq()) {
                String analyzedValue = analyze(field, value.toString(), schema.analyzer);
                if (analyzedValue == null) {
                    throw new IllegalArgumentException("Value discarded by analyzer");
                }
                Term term = new Term(field, analyzedValue);
                query = new TermQuery(term);
            } else if (fieldType == Properties.Type.integer) {
                assert numericConfig != null;
                Integer value = (Integer) numericConfig.getNumberFormat().parse(this.value.toString());
                query = NumericRangeQuery.newIntRange(field, value, value, true, true);
            } else if (fieldType == Properties.Type.bigint || fieldType == Properties.Type.date) {
                assert numericConfig != null;
                Long value = (Long) numericConfig.getNumberFormat().parse(this.value.toString());
                query = NumericRangeQuery.newLongRange(field, value, value, true, true);
            } else if (fieldType == Properties.Type.decimal) {
                assert numericConfig != null;
                Float value = (Float) numericConfig.getNumberFormat().parse(this.value.toString());
                query = NumericRangeQuery.newFloatRange(field, value, value, true, true);
            } else if (fieldType == Properties.Type.bigdecimal) {
                assert numericConfig != null;
                Double value = (Double) numericConfig.getNumberFormat().parse(this.value.toString());
                query = NumericRangeQuery.newDoubleRange(field, value, value, true, true);
            } else {
                String message = String.format("Match queries are not supported by %s field type", fieldType);
                throw new UnsupportedOperationException(message);
            }
            query.setBoost(boost);
            return query;
        }
        String message = String.format("Match queries cannot be supported until mapping is defined");
        throw new UnsupportedOperationException(message);
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

}