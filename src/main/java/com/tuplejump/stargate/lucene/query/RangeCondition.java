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

import com.tuplejump.stargate.lucene.*;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

/**
 * A {@link Condition} implementation that matches a field within an range of values.
 */
public class RangeCondition extends Condition {


    private final String format;
    /**
     * The field name.
     */
    private final String field;

    /**
     * The lower field value included in the range.
     */
    private Object lower;

    /**
     * The upper field value included in the range.
     */
    private Object upper;

    /**
     * If the lower value is included in the range.
     */
    private final boolean includeLower;

    /**
     * If the upper value is included in the range.
     */
    private final boolean includeUpper;

    /**
     * Constructs a query selecting all fields greater/equal than {@code lowerValue} but less/equal than
     * {@code upperValue}.
     * If an endpoint is null, it is said to be "open". Either or both endpoints may be open. Open endpoints may not be
     * exclusive (you can't select all but the first or last term without explicitly specifying the term to exclude.)
     *
     * @param boost        The boost for this query clause. Documents matching this clause will (in addition to the normal
     *                     weightings) have their score multiplied by {@code boost}. If {@code null}, then DEFAULT_BOOST
     *                     is used as default.
     * @param field        the field name.
     * @param format       the format if the field is a date
     * @param lowerValue   the field value at the lower end of the range.
     * @param upperValue   the field value at the upper end of the range.
     * @param includeLower if {@code true}, the {@code lowerValue} is included in the range.
     * @param includeUpper if {@code true}, the {@code upperValue} is included in the range.
     */
    @JsonCreator
    public RangeCondition(@JsonProperty("boost") Float boost,
                          @JsonProperty("field") String field,
                          @JsonProperty("format") String format,
                          @JsonProperty("lower") Object lowerValue,
                          @JsonProperty("upper") Object upperValue,
                          @JsonProperty("includeLower") boolean includeLower,
                          @JsonProperty("includeUpper") boolean includeUpper) {
        super(boost);

        this.field = field != null ? field.toLowerCase() : null;
        this.lower = lowerValue;
        this.upper = upperValue;
        this.includeLower = includeLower;
        this.includeUpper = includeUpper;
        this.format = format;
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
     * Returns the field value at the lower end of the range.
     *
     * @return the field value at the lower end of the range.
     */
    public Object getLowerValue() {
        return lower;
    }

    /**
     * Returns the field value at the upper end of the range.
     *
     * @return the field value at the upper end of the range.
     */
    public Object getUpperValue() {
        return upper;
    }

    /**
     * Returns {@code true} if the {@code lowerValue} is included in the range, {@code false} otherwise.
     *
     * @return {@code true} if the {@code lowerValue} is included in the range, {@code false} otherwise.
     */
    public boolean getIncludeLower() {
        return includeLower;
    }

    /**
     * Returns {@code true} if the {@code includeUpper} is included in the range, {@code false} otherwise.
     *
     * @return {@code true} if the {@code includeUpper} is included in the range, {@code false} otherwise.
     */
    public boolean getIncludeUpper() {
        return includeUpper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query query(Options schema) throws Exception {
        Query query;
        if (field == null || field.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name required");
        }
        NumericConfig numericConfig = schema.numericFieldOptions.get(field);

        Properties properties = schema.getProperties(field);
        Type fieldType = properties != null ? properties.getType() : Type.text;
        //TODO Range on TimeUUID type
        if (fieldType.isCharSeq()) {
            String lowerVal = null, upperVal = null;
            if (this.lower != null) {
                lowerVal = analyze(field, this.lower.toString(), schema.analyzer);
            }
            if (this.upper != null) {
                upperVal = analyze(field, this.upper.toString(), schema.analyzer);
            }
            query = TermRangeQuery.newStringRange(field, lowerVal, upperVal, includeLower, includeUpper);
        } else if (fieldType == Type.integer) {
            assert numericConfig != null;
            Integer lower = this.lower == null ? Integer.MIN_VALUE : numericConfig.getNumberFormat().parse(this.lower.toString()).intValue();
            Integer upper = this.upper == null ? Integer.MAX_VALUE : numericConfig.getNumberFormat().parse(this.upper.toString()).intValue();
            query = NumericRangeQuery.newIntRange(field, lower, upper, includeLower, includeUpper);
        } else if (fieldType == Type.bigint) {
            assert numericConfig != null;
            Long lower = this.lower == null ? Long.MIN_VALUE : numericConfig.getNumberFormat().parse(this.lower.toString()).longValue();
            Long upper = this.upper == null ? Long.MAX_VALUE : numericConfig.getNumberFormat().parse(this.upper.toString()).longValue();
            query = NumericRangeQuery.newLongRange(field, lower, upper, includeLower, includeUpper);
        } else if (fieldType == Type.decimal) {
            assert numericConfig != null;
            Float lower = this.lower == null ? Float.MIN_VALUE : numericConfig.getNumberFormat().parse(this.lower.toString()).floatValue();
            Float upper = this.upper == null ? Float.MAX_VALUE : numericConfig.getNumberFormat().parse(this.upper.toString()).floatValue();
            query = NumericRangeQuery.newFloatRange(field, lower, upper, includeLower, includeUpper);
        } else if (fieldType == Type.bigdecimal) {
            assert numericConfig != null;
            Double lower = this.lower == null ? Double.MIN_VALUE : numericConfig.getNumberFormat().parse(this.lower.toString()).doubleValue();
            Double upper = this.upper == null ? Double.MAX_VALUE : numericConfig.getNumberFormat().parse(this.upper.toString()).doubleValue();
            query = NumericRangeQuery.newDoubleRange(field, lower, upper, includeLower, includeUpper);
        } else if (fieldType == Type.date) {
            Long lower;
            Long upper;
            if ("millis".equals(format)) {
                lower = this.lower == null ? Long.MIN_VALUE : Long.valueOf(this.lower.toString());
                upper = this.upper == null ? Long.MAX_VALUE : Long.valueOf(this.upper.toString());
            } else {
                FormatDateTimeFormatter formatter = Dates.forPattern(format, Locale.getDefault());
                DateTimeFormatter parser = formatter.parser();
                lower = this.lower == null ? Long.MIN_VALUE : parser.parseMillis(this.lower.toString());
                upper = this.upper == null ? Long.MAX_VALUE : parser.parseMillis(this.upper.toString());
            }
            query = NumericRangeQuery.newLongRange(field, lower, upper, includeLower, includeUpper);
        } else {
            String message = String.format("Range queries are not supported by %s mapper", fieldType);
            throw new UnsupportedOperationException(message);
        }
        return query;
    }

    @Override
    public String getType() {
        return "range";
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
        builder.append(", lowerValue=");
        builder.append(lower);
        builder.append(", upperValue=");
        builder.append(upper);
        builder.append(", includeLower=");
        builder.append(includeLower);
        builder.append(", includeUpper=");
        builder.append(includeUpper);
        builder.append("]");
        return builder.toString();
    }

}
