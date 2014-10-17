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
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A per field sort order by.
 */
public class SortField {

    private static final boolean DEFAULT_REVERSE = false;

    /**
     * The name of field to sort by.
     */
    private String field;

    /**
     * {@code true} if natural order should be reversed.
     */
    private boolean reverse;

    /**
     * Returns a new {@link SortField}.
     *
     * @param field   The name of field to sort by.
     * @param reverse {@code true} if natural order should be reversed.
     */
    @JsonCreator
    public SortField(@JsonProperty("field") String field, @JsonProperty("reverse") Boolean reverse) {
        this.field = field != null ? field.toLowerCase() : null;
        this.reverse = reverse == null ? DEFAULT_REVERSE : reverse;
    }

    /**
     * Returns the Lucene's {@link org.apache.lucene.search.SortField} representing this {@link SortField}.
     *
     * @param schema
     * @return the Lucene's {@link org.apache.lucene.search.SortField} representing this {@link SortField}.
     */
    public org.apache.lucene.search.SortField sortField(Options schema) {
        if (field == null || field.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name required");
        }
        Properties properties = schema.getProperties(field);
        if (properties == null) properties = Properties.ID_FIELD;
        return sortField(field, properties, reverse);
    }


    public static org.apache.lucene.search.SortField sortField(String name, Properties properties, boolean reverse) {
        Properties.Type cqlType = properties.getType();
        if (cqlType == Properties.Type.integer) {
            return new org.apache.lucene.search.SortField(name, org.apache.lucene.search.SortField.Type.INT, reverse);
        } else if (cqlType == Properties.Type.bigint) {
            return new org.apache.lucene.search.SortField(name, org.apache.lucene.search.SortField.Type.LONG, reverse);
        } else if (cqlType == Properties.Type.bigdecimal) {
            return new org.apache.lucene.search.SortField(name, org.apache.lucene.search.SortField.Type.DOUBLE, reverse);
        } else if (cqlType == Properties.Type.decimal) {
            return new org.apache.lucene.search.SortField(name, org.apache.lucene.search.SortField.Type.FLOAT, reverse);
        } else {
            return new org.apache.lucene.search.SortField(name, org.apache.lucene.search.SortField.Type.STRING, reverse);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sort [field=");
        builder.append(field);
        builder.append(", reverse=");
        builder.append(reverse);
        builder.append("]");
        return builder.toString();
    }

}
