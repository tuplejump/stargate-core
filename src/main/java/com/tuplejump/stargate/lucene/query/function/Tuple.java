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


import com.tuplejump.stargate.lucene.Constants;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * User: satya
 */
public class Tuple extends BaseVariableResolverFactory {

    Map<String, Integer> positions;
    Object[] tuple;
    boolean[] simpleExpressions;
    Set<String> nestedFields;

    public Tuple(Set<String> nestedFields, Map<String, Integer> positions, boolean[] simpleExpressions) {
        this.positions = positions;
        this.simpleExpressions = simpleExpressions;
        this.nestedFields = nestedFields;
        tuple = new Object[positions.size()];
        this.indexedVariableNames = new String[positions.size()];
        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            indexedVariableNames[entry.getValue()] = entry.getKey();
        }
    }

    public Object[] getTuple() {
        return tuple;
    }

    public void setValue(String field, Object value) {
        tuple[this.positions.get(field)] = value;
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
        Tuple retVal = new Tuple(nestedFields, newPositions, simpleExpressions);
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


    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            String field = entry.getKey();
            generator.writeFieldName(field);
            generator.writeString(tuple[entry.getValue()].toString());
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
            if (nestedFields.contains(name)) {
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
        if (this.nestedFields.contains(name)) return true;
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


}
