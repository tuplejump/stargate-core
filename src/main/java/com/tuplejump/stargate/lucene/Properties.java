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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexOptions;
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
    public static Version luceneVersion = Version.LUCENE_5_5_0;

    static {
        ID_FIELD.tokenized = false;
        ID_FIELD.indexed = true;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public Striped getStriped() {
        return striped;
    }

    public enum Striped {
        also, only, none
    }

    @JsonProperty
    boolean nearRealTime = false;

    @JsonProperty
    boolean metaColumn = true;

    @JsonProperty
    private
    Type type;

    @JsonProperty
    private
    String analyzer;

    @JsonProperty
    Boolean indexed = true;

    @JsonProperty
    Boolean stored = false;

    @JsonProperty
    private
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
    IndexOptions indexOptions = IndexOptions.DOCS;

    @JsonProperty
    int numericPrecisionStep = NumericUtils.PRECISION_STEP_DEFAULT;

    @JsonProperty
    private
    Map<String, Properties> fields = new HashMap<>();

    boolean lowerCased;

    public Type getType() {
        if (type == null) {
            if (getFields() != null && !getFields().isEmpty()) return Type.object;
        }
        return type;
    }

    Analyzer analyzerObj;

    public Analyzer getLuceneAnalyzer() {
        if (analyzerObj == null) {
            if (getAnalyzer() == null) {
                if (getType() != null && !getType().canTokenize())
                    analyzerObj = AnalyzerFactory.getAnalyzer(AnalyzerFactory.Analyzers.KeywordAnalyzer.name());
                else
                    analyzerObj = AnalyzerFactory.getAnalyzer(AnalyzerFactory.Analyzers.StandardAnalyzer.name());
            } else {
                analyzerObj = AnalyzerFactory.getAnalyzer(getAnalyzer());
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
                if (val.getType() != null) {
                    if (val.getType() == Type.object) {
                        dynamicNumericConfigMap.putAll(val.getDynamicNumericConfig());
                    } else if (val.getType().isNumeric()) {
                        dynamicNumericConfigMap.put(colName, LuceneUtils.numericConfig(val.dynamicFieldType()));
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
                dynamicFieldType = LuceneUtils.dynamicFieldType(this);
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
        return getStriped();
    }

    public boolean isNearRealTime() {
        return nearRealTime;
    }

    public void setNearRealTime(boolean nearRealTime) {
        this.nearRealTime = nearRealTime;
    }

    public boolean isTokenized() {
        if (tokenized == null) {
            if (getType() != null && getType().canTokenize())
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

    public IndexOptions getIndexOptions() {
        return indexOptions;
    }

    public int getNumericPrecisionStep() {
        return numericPrecisionStep;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public void setFields(Map<String, Properties> mapping) {
        this.fields = mapping;
    }

    public Map<String, Analyzer> perFieldAnalyzers() {
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        if (getFields() != null) {
            for (Map.Entry<String, Properties> fieldOptions : getFields().entrySet()) {
                String colName = fieldOptions.getKey();
                Properties props = fieldOptions.getValue();
                if (props.getType() == Type.object || props.getType() == Type.map) {
                    Map<String, Analyzer> fieldObjectAnalyzers = props.perFieldAnalyzers();
                    for (Map.Entry<String, Analyzer> entry : fieldObjectAnalyzers.entrySet()) {
                        perFieldAnalyzers.put(colName + "." + entry.getKey(), entry.getValue());
                    }
                } else {
                    perFieldAnalyzers.put(fieldOptions.getKey(), fieldOptions.getValue().getLuceneAnalyzer());
                }
            }
        }
        return perFieldAnalyzers;
    }

}
