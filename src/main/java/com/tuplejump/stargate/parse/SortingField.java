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
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.lucene.search.SortField;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A sorting for a field of a search.
 */
public class SortingField {

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
     * Returns a new {@link SortingField}.
     *
     * @param field   The name of field to sort by.
     * @param reverse {@code true} if natural order should be reversed.
     */
    @JsonCreator
    public SortingField(@JsonProperty("field") String field, @JsonProperty("reverse") Boolean reverse) {
        this.field = field != null ? field.toLowerCase() : null;
        this.reverse = reverse == null ? DEFAULT_REVERSE : reverse;
    }

    /**
     * Returns the Lucene's {@link SortField} representing this {@link SortingField}.
     *
     * @param schema
     * @return the Lucene's {@link SortField} representing this {@link SortingField}.
     */
    public SortField sortField(Options schema) {
        if (field == null || field.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name required");
        }
        AbstractType fieldType = schema.validators.get(field);
        return sortField(field, fieldType, reverse);
    }


    public static CQL3SortField sortField(String name, AbstractType type, boolean reverse) {
        CQL3Type cqlType = type.asCQL3Type();
        if (cqlType == CQL3Type.Native.INT) {
            return new CQL3SortField(name, SortField.Type.INT, reverse, cqlType);
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            return new CQL3SortField(name, SortField.Type.LONG, reverse, cqlType);
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            return new CQL3SortField(name, SortField.Type.DOUBLE, reverse, cqlType);
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            return new CQL3SortField(name, SortField.Type.FLOAT, reverse, cqlType);
        } else if (cqlType == CQL3Type.Native.TEXT || cqlType == CQL3Type.Native.VARCHAR) {
            return new CQL3SortField(name, SortField.Type.STRING_VAL, reverse, cqlType);
        } else if (cqlType == CQL3Type.Native.UUID) {
            return new CQL3SortField(name, SortField.Type.STRING, reverse, cqlType);
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            return new CQL3SortField(name, SortField.Type.LONG, reverse, cqlType);
        } else if (cqlType == CQL3Type.Native.BOOLEAN) {
            return new CQL3SortField(name, SortField.Type.SCORE, reverse, cqlType);
        } else {
            return new CQL3SortField(name, SortField.Type.BYTES, reverse, cqlType);
        }
    }

    public static class CQL3SortField extends SortField {
        public CQL3Type cql3Type;

        public CQL3SortField(String field, Type type, boolean reverse, CQL3Type cql3Type) {
            super(field, type, reverse);
            this.cql3Type = cql3Type;
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
