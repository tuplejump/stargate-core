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

import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.cassandra.IndexEntryCollector;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: satya
 */
public class Tuple {

    Map<String, Integer> positions;
    Map<String, AbstractType> validators;
    Object[] tuple;

    public Tuple(Map<String, Integer> positions, Map<String, AbstractType> validators) {
        this.positions = positions;
        this.validators = validators;
        tuple = new Object[positions.size()];
    }


    public void setValuesFromIndexEntry(IndexEntryCollector.IndexEntry entry) {
        for (String field : positions.keySet()) {
            AbstractType abstractType = validators.get(field);
            if (abstractType != null) {
                if (isNumber(abstractType.asCQL3Type())) {
                    tuple[this.positions.get(field)] = entry.getNumber(field);
                } else {
                    tuple[this.positions.get(field)] = entry.getByteBuffer(field);
                }
            }
        }
    }

    public void setValuesFromRow(Row row, ColumnFamilyStore table) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        ColumnFamily cf = row.cf;
        Collection<Column> cols = cf.getSortedColumns();
        for (Column column : cols) {
            String actualColumnName = Utils.getColumnNameStr(baseComparator, column.name());
            ByteBuffer colValue = column.value();
            AbstractType<?> valueValidator = table.metadata.getValueValidatorFromColumnName(column.name());
            if (valueValidator.isCollection()) {
                CollectionType validator = (CollectionType) valueValidator;
                AbstractType keyType = validator.nameComparator();
                AbstractType valueType = validator.valueComparator();
                ByteBuffer[] components = baseComparator.split(column.name());
                ByteBuffer keyBuf = components[components.length - 1];
                if (valueValidator instanceof MapType) {
                    actualColumnName = actualColumnName + "." + keyType.compose(keyBuf);
                    valueValidator = valueType;
                } else if (valueValidator instanceof SetType) {
                    colValue = keyBuf;
                    valueValidator = keyType;
                } else {
                    valueValidator = valueType;
                }
            }
            for (String field : positions.keySet()) {
                if (actualColumnName.equalsIgnoreCase(field)) {
                    if (isNumber(valueValidator.asCQL3Type())) {
                        tuple[this.positions.get(field)] = valueValidator.compose(colValue);
                    } else {
                        tuple[this.positions.get(field)] = colValue;
                    }
                }
            }
        }
    }

    public Object getValue(String field) {
        return tuple[this.positions.get(field)];
    }

    public Tuple getView(String[] columns) {
        Object[] newTuple = null;
        Map<String, Integer> newPositions = new HashMap<>();
        if (columns != null) {
            newTuple = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];
                int position = positions.get(col);
                newTuple[i] = tuple[position];
                newPositions.put(col, i);
            }
        }
        Tuple retVal = new Tuple(newPositions, validators);
        retVal.tuple = newTuple;
        return retVal;
    }

    @Override
    public boolean equals(Object obj) {
        Tuple other = ((Tuple) obj);
        if (tuple == null && other.tuple == null) return true;
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        for (int i = 0; i < tuple.length; i++) {
            equalsBuilder.append(tuple[i], other.tuple[i]);
        }
        return equalsBuilder.build();
    }

    @Override
    public int hashCode() {
        if (tuple == null) return 0;
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (int i = 0; i < tuple.length; i++) {
            hashCodeBuilder.append(tuple[i]);
        }
        return hashCodeBuilder.build();
    }


    public static boolean isNumber(CQL3Type cqlType) {
        if (cqlType == CQL3Type.Native.INT || cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT ||
                cqlType == CQL3Type.Native.COUNTER || cqlType == CQL3Type.Native.DECIMAL
                || cqlType == CQL3Type.Native.DOUBLE || cqlType == CQL3Type.Native.FLOAT)
            return true;
        return false;
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            String field = entry.getKey();
            generator.writeFieldName(field);
            AbstractType validator = validators.get(field);
            if (isNumber(validator.asCQL3Type())) {
                generator.writeNumber(((Number) tuple[entry.getValue()]).doubleValue());
            } else {
                generator.writeString(validator.getString((ByteBuffer) tuple[entry.getValue()]));
            }
        }
        generator.writeEndObject();
    }

}
