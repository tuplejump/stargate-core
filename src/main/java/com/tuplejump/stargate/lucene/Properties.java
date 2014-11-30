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

package com.tuplejump.stargate.lucene;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.Utils;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * User: satya
 * The properties of one field.
 * If the field is of Object type(i.e Json) then the field properties inside the json can be mapped recursively
 * using the "fields" field.
 */
public class Properties {

    public static Properties ID_FIELD = new Properties();
    public static Version luceneVersion = Version.LUCENE_48;

    static {
        ID_FIELD.tokenized = false;
        ID_FIELD.indexed = true;
    }

    public enum Striped {
        also, only, none
    }

    public enum Type {
        object,
        map,
        text,
        string,
        integer,
        bigint,
        decimal,
        bigdecimal,
        bool,
        date;

        public boolean isNumeric() {
            return this == bigint || this == bigdecimal || this == integer || this == decimal;
        }

        public boolean isCharSeq() {
            return this == string || this == text;
        }

        public boolean canTokenize() {
            Type type = this;
            return !(type.isNumeric() || type == Type.string || type == Type.date || type == Type.bool);
        }


    }

    @JsonProperty
    boolean nearRealTime = false;

    @JsonProperty
    boolean metaColumn = true;

    @JsonProperty
    Type type;

    @JsonProperty
    String analyzer;

    @JsonProperty
    Boolean indexed = true;

    @JsonProperty
    Boolean stored = false;

    @JsonProperty
    Striped striped = Striped.none;

    @JsonProperty
    Boolean tokenized = true;

    @JsonProperty
    Boolean omitNorms;

    @JsonProperty
    int maxFieldLength;

    @JsonProperty
    Boolean storeTermVectors;

    @JsonProperty
    Boolean storeTermVectorOffsets;

    @JsonProperty
    Boolean storeTermVectorPositions;

    @JsonProperty
    Boolean storeTermVectorPayloads;


    @JsonProperty
    FieldInfo.IndexOptions indexOptions = FieldInfo.IndexOptions.DOCS_ONLY;

    @JsonProperty
    int numericPrecisionStep = NumericUtils.PRECISION_STEP_DEFAULT;

    @JsonProperty
    Map<String, Properties> fields = new HashMap<>();

    boolean lowerCased;

    public Type getType() {
        if (type == null) {
            if (fields != null && !fields.isEmpty()) return Type.object;
        }
        return type;
    }

    Analyzer analyzerObj;

    public Analyzer getAnalyzer() {
        if (analyzerObj == null) {
            if (analyzer == null) {
                if (type != null && !type.canTokenize())
                    analyzerObj = AnalyzerFactory.getAnalyzer(AnalyzerFactory.Analyzers.KeywordAnalyzer.name(), Properties.luceneVersion);
                else
                    analyzerObj = AnalyzerFactory.getAnalyzer(AnalyzerFactory.Analyzers.StandardAnalyzer.name(), Properties.luceneVersion);
            } else {
                analyzerObj = AnalyzerFactory.getAnalyzer(analyzer, Properties.luceneVersion);
            }
        }
        return analyzerObj;
    }

    public Map<String, Properties> getFields() {
        if (!lowerCased) {
            Map<String, Properties> lcFields = new HashMap<>();
            for (Map.Entry<String, Properties> entry : fields.entrySet()) {
                lcFields.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            fields = lcFields;
            lowerCased = true;
        }
        return fields;
    }

    public Map<String, NumericConfig> getDynamicNumericConfig() {
        if (dynamicNumericConfigMap == null) {
            dynamicNumericConfigMap = new HashMap<>();
            Map<String, Properties> fields = getFields();
            for (Map.Entry<String, Properties> entry : fields.entrySet()) {
                Properties val = entry.getValue();
                String colName = entry.getKey();
                if (val.type != null) {
                    if (val.type == Type.object) {
                        dynamicNumericConfigMap.putAll(val.getDynamicNumericConfig());
                    } else if (val.type.isNumeric()) {
                        dynamicNumericConfigMap.put(colName, Utils.numericConfig(val.dynamicFieldType()));
                    }
                }
            }
        }
        return dynamicNumericConfigMap;
    }

    private FieldType dynamicFieldType;
    private Map<String, NumericConfig> dynamicNumericConfigMap;

    public FieldType dynamicFieldType() {
        if (dynamicFieldType == null) {
            if (!(getType() == Type.object)) {
                dynamicFieldType = dynamicFieldType(this);
            }
        }
        return dynamicFieldType;
    }

    public boolean isIndexed() {
        return indexed != null ? indexed : true;
    }

    public boolean isStored() {
        return stored != null ? stored : false;
    }

    public Striped striped() {
        return striped;
    }

    public boolean isNearRealTime() {
        return nearRealTime;
    }

    public void setNearRealTime(boolean nearRealTime) {
        this.nearRealTime = nearRealTime;
    }

    public boolean isTokenized() {
        if (tokenized == null) {
            if (type != null && type.canTokenize())
                return true;
            return false;
        }
        return tokenized;
    }

    public boolean isMetaColumn() {
        return metaColumn;
    }

    public int getMaxFieldLength() {
        return maxFieldLength;
    }

    public boolean isStoreTermVectors() {
        return storeTermVectors != null ? storeTermVectors : false;
    }

    public boolean isStoreTermVectorOffsets() {
        return storeTermVectorOffsets != null ? storeTermVectorOffsets : false;
    }

    public boolean isStoreTermVectorPositions() {
        return storeTermVectorPositions != null ? storeTermVectorPositions : false;
    }

    public boolean isStoreTermVectorPayloads() {
        return storeTermVectorPayloads != null ? storeTermVectorPayloads : false;
    }

    public boolean isOmitNorms() {
        return omitNorms != null ? omitNorms : true;
    }

    public FieldInfo.IndexOptions getIndexOptions() {
        return indexOptions;
    }

    public int getNumericPrecisionStep() {
        return numericPrecisionStep;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setFromAbstractType(AbstractType type) {
        if (this.type != null) return;
        CQL3Type cqlType = type.asCQL3Type();
        if (cqlType == CQL3Type.Native.INT) {
            this.type = Type.integer;
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            this.type = Type.bigint;
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            this.type = Type.bigdecimal;
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            this.type = Type.decimal;
        } else if (cqlType == CQL3Type.Native.TEXT || cqlType == CQL3Type.Native.ASCII) {
            this.type = Type.text;
        } else if (cqlType == CQL3Type.Native.VARCHAR) {
            this.type = Type.string;
        } else if (cqlType == CQL3Type.Native.UUID) {
            this.type = Type.string;
        } else if (cqlType == CQL3Type.Native.TIMEUUID) {
            //TODO TimeUUID toString is not comparable.
            this.type = Type.string;
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            this.type = Type.date;
        } else if (cqlType == CQL3Type.Native.BOOLEAN) {
            this.type = Type.bool;
        } else {
            this.type = Type.text;
        }

    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public void setFields(Map<String, Properties> mapping) {
        this.fields = mapping;
    }

    public Map<String, Analyzer> perFieldAnalyzers() {
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        if (fields != null) {
            for (Map.Entry<String, Properties> fieldOptions : fields.entrySet()) {
                String colName = fieldOptions.getKey();
                Properties props = fieldOptions.getValue();
                if (props.getType() == Properties.Type.object || props.getType() == Type.map) {
                    Map<String, Analyzer> fieldObjectAnalyzers = props.perFieldAnalyzers();
                    for (Map.Entry<String, Analyzer> entry : fieldObjectAnalyzers.entrySet()) {
                        perFieldAnalyzers.put(colName + "." + entry.getKey(), entry.getValue());
                    }
                } else {
                    perFieldAnalyzers.put(fieldOptions.getKey(), fieldOptions.getValue().getAnalyzer());
                }
            }
        }
        return perFieldAnalyzers;
    }

    public static FieldType docValueTypeFrom(FieldType fieldType) {
        FieldType docValType = new FieldType(fieldType);
        if (fieldType.numericType() != null) docValType.setDocValueType(FieldInfo.DocValuesType.NUMERIC);
        else docValType.setDocValueType(FieldInfo.DocValuesType.BINARY);
        return docValType;
    }


    public static FieldType fieldType(Properties properties, AbstractType validator) {
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(properties.isIndexed());
        fieldType.setTokenized(properties.isTokenized());
        fieldType.setStored(properties.isStored());
        fieldType.setStoreTermVectors(properties.isStoreTermVectors());
        fieldType.setStoreTermVectorOffsets(properties.isStoreTermVectorOffsets());
        fieldType.setStoreTermVectorPayloads(properties.isStoreTermVectorPayloads());
        fieldType.setStoreTermVectorPositions(properties.isStoreTermVectorPositions());
        fieldType.setOmitNorms(properties.isOmitNorms());
        fieldType.setIndexOptions(properties.getIndexOptions());
        Fields.setNumericType(validator, fieldType);
        if (fieldType.numericType() != null) {
            fieldType.setNumericPrecisionStep(properties.getNumericPrecisionStep());
        }
        return fieldType;
    }

    public static FieldType dynamicFieldType(Properties properties) {
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(properties.isIndexed());
        fieldType.setTokenized(properties.isTokenized());
        fieldType.setStored(properties.isStored());
        fieldType.setStoreTermVectors(properties.isStoreTermVectors());
        fieldType.setStoreTermVectorOffsets(properties.isStoreTermVectorOffsets());
        fieldType.setStoreTermVectorPayloads(properties.isStoreTermVectorPayloads());
        fieldType.setStoreTermVectorPositions(properties.isStoreTermVectorPositions());
        fieldType.setOmitNorms(properties.isOmitNorms());
        fieldType.setIndexOptions(properties.getIndexOptions());
        if (properties.getType().isNumeric()) {
            switch (properties.getType()) {
                case integer:
                    fieldType.setNumericType(FieldType.NumericType.INT);
                    break;
                case bigint:
                    fieldType.setNumericType(FieldType.NumericType.LONG);
                    break;
                case decimal:
                    fieldType.setNumericType(FieldType.NumericType.FLOAT);
                    break;
                default:
                    fieldType.setNumericType(FieldType.NumericType.DOUBLE);
                    break;
            }
            fieldType.setNumericPrecisionStep(properties.getNumericPrecisionStep());
        }
        return fieldType;
    }

}
