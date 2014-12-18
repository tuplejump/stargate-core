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

import com.tuplejump.stargate.Constants;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.cassandra.IndexEntryCollector;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.utils.Pair;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonGenerator;
import org.mvel2.MVEL;
import org.mvel2.UnresolveablePropertyException;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.impl.BaseVariableResolverFactory;
import org.mvel2.integration.impl.SimpleSTValueResolver;
import org.mvel2.integration.impl.SimpleValueResolver;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 */
public class Tuple extends BaseVariableResolverFactory {

    Map<String, Integer> positions;
    Map<String, AbstractType> validators;
    Object[] tuple;
    boolean[] simpleExpressions;
    Options options;

    public Tuple(Options options, Map<String, Integer> positions, Map<String, AbstractType> validators, boolean[] simpleExpressions) {
        this.options = options;
        this.positions = positions;
        this.validators = validators;
        this.simpleExpressions = simpleExpressions;
        tuple = new Object[positions.size()];
        this.indexedVariableNames = new String[positions.size()];
        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            indexedVariableNames[entry.getValue()] = entry.getKey();
        }
    }


    public void load(IndexEntryCollector.IndexEntry entry) {
        for (String field : positions.keySet()) {
            AbstractType validator = getFieldValidator(field);
            if (validator != null) {
                CQL3Type type = validator.asCQL3Type();
                if (isNumber(type)) {
                    tuple[this.positions.get(field)] = entry.getNumber(field);
                } else if (type == CQL3Type.Native.TIMESTAMP) {
                    Number number = entry.getNumber(field);
                    tuple[this.positions.get(field)] = new Date(number.longValue());
                } else {
                    tuple[this.positions.get(field)] = validator.compose(entry.getByteBuffer(field));
                }
            }
        }
    }

    public void load(Row row, ColumnFamilyStore table) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        CFDefinition cfDef = table.metadata.getCfDef();
        ColumnFamily cf = row.cf;
        ByteBuffer rowKey = row.key.key;
        AbstractType<?> keyValidator = table.metadata.getKeyValidator();

        Collection<Column> cols = cf.getSortedColumns();
        boolean keyColumnsAdded = false;
        for (Column column : cols) {
            if (!keyColumnsAdded) {
                ByteBuffer[] keyComponents = cfDef.hasCompositeKey ? ((CompositeType) table.metadata.getKeyValidator()).split(rowKey) : new ByteBuffer[]{rowKey};
                List<AbstractType<?>> keyValidators = keyValidator.getComponents();
                for (Map.Entry<Integer, Pair<String, ByteBuffer>> entry : options.partitionKeysIndexed.entrySet()) {
                    ByteBuffer value = keyComponents[entry.getKey()];
                    AbstractType<?> validator = keyValidators.get(entry.getKey());
                    String actualColumnName = entry.getValue().left;
                    for (String field : positions.keySet()) {
                        if (actualColumnName.equalsIgnoreCase(field)) {
                            tuple[this.positions.get(field)] = validator.compose(value);
                        }
                    }
                }
            }
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
                    tuple[this.positions.get(field)] = valueValidator.compose(colValue);
                }
            }
        }
    }

    public Object getValue(String field) {
        return tuple[this.positions.get(field)];
    }

    public Tuple project(String[] columns, Serializable[] groupByExpressions) {
        Map<String, Integer> newPositions = new HashMap<>();
        Object[] newTuple = columns != null ? new Object[columns.length] : null;
        if (groupByExpressions != null) {
            for (int i = 0; i < groupByExpressions.length; i++) {
                String col = columns[i];
                if (simpleExpressions[i]) {
                    int position = positions.get(col);
                    newTuple[i] = tuple[position];
                } else {
                    newTuple[i] = MVEL.executeExpression(groupByExpressions[i], this);
                }
                newPositions.put(col, i);
            }
        }
        Tuple retVal = new Tuple(options, newPositions, validators, simpleExpressions);
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
            AbstractType validator = getFieldValidator(field);
            Object value = tuple[entry.getValue()];
            if (validator != null && isNumber(validator.asCQL3Type()) && value != null) {
                generator.writeNumber(((Number) value).doubleValue());
            } else {
                generator.writeString(value == null ? null : value.toString());
            }
        }
        generator.writeEndObject();
    }

    public VariableResolver createVariable(String name, Object value) {
        VariableResolver vr = getResolver(name);
        if (vr != null) {
            vr.setValue(value);
        }
        return vr;
    }

    public VariableResolver createVariable(String name, Object value, Class<?> type) {
        return createVariable(name, value);
    }

    public VariableResolver getVariableResolver(String name) {
        VariableResolver vr = getResolver(name);
        if (vr != null) return vr;
        else if (nextFactory != null) {
            return nextFactory.getVariableResolver(name);
        }

        throw new UnresolveablePropertyException("unable to resolve variable '" + name + "'");
    }

    public boolean isResolveable(String name) {
        return isTarget(name) || (nextFactory != null && nextFactory.isResolveable(name));
    }

    private VariableResolver getResolver(String name) {
        Integer pos = this.positions.get(name);
        if (pos != null) return new SimpleValueResolver(tuple[pos]);
        else {
            AbstractType validator = this.validators.get(name);
            if (validator != null && validator instanceof CollectionType) {
                Map variables = new HashMap();
                for (Map.Entry<String, Integer> field : positions.entrySet()) {
                    if (field.getKey() != null) {
                        Iterator<String> nestedProps = Constants.dotSplitter.split(field.getKey()).iterator();
                        if (name.equalsIgnoreCase(nestedProps.next())) {
                            variables.put(nestedProps.next(), tuple[field.getValue()]);
                        }
                    }
                }
                return new SimpleSTValueResolver(variables, Map.class);
            }
        }
        return null;
    }

    public boolean isTarget(String name) {
        if (this.validators.get(name) != null) return true;
        Integer pos = this.positions.get(name);
        return pos != null ? true : false;
    }

    public Set<String> getKnownVariables() {
        return positions.keySet();
    }

    @Override
    public boolean isIndexedFactory() {
        return false;
    }

    public AbstractType getFieldValidator(String field) {
        AbstractType abstractType = validators.get(field);
        return AggregateFunction.getValueValidator(abstractType);
    }

}
