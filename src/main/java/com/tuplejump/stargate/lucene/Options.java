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

import com.google.common.base.Splitter;
import com.tuplejump.stargate.cassandra.CassandraUtils;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * User: satya
 * <p>
 * This is used to get index options and field options to apply to the lucene based cassandra secondary indexes.
 */
public class Options implements Serializable {
    public static final Logger logger = LoggerFactory.getLogger(Options.class);
    public static final String DUMMY_DIR = "_DUMMY_";
    public static String defaultIndexesDir = System.getProperty("sg.index.dir", DUMMY_DIR);


    public static final ObjectMapper inputMapper = new ObjectMapper();

    static {
        inputMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        inputMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        inputMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        inputMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        inputMapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
        SimpleModule module = new SimpleModule("LowerCaseKeyDeserializer",
                new org.codehaus.jackson.Version(1, 9, 0, null));
        module.addKeyDeserializer(Object.class, new LowerCaseKeyDeserializer());
        module.addKeyDeserializer(Map.class, new LowerCaseKeyDeserializer());
        module.addSerializer(FieldType.class, new FieldTypeSerializer());
        module.addSerializer(Analyzer.class, new AnalyzerSerializer());
        inputMapper.registerModule(module);


        if (defaultIndexesDir.equals(DUMMY_DIR)) {
            try {
                String dataDir = CassandraUtils.getDataDirs()[0];
                if (!dataDir.endsWith(File.separator)) {
                    dataDir = dataDir + File.separator;
                }
                defaultIndexesDir = dataDir + "sgindex";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class FieldTypeSerializer extends JsonSerializer<FieldType> {
        @Override
        public void serialize(FieldType value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
            gen.writeString(value.toString());
        }
    }

    public static class AnalyzerSerializer extends JsonSerializer<Analyzer> {
        @Override
        public void serialize(Analyzer value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
            gen.writeString(value.getClass().getName());
        }
    }

    static class LowerCaseKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctx)
                throws IOException {
            return key.toLowerCase();
        }
    }


    public final Map<String, Properties> fields;
    public final Properties primary;
    public final Map<String, NumericConfig> numericFieldOptions;
    public final Map<String, FieldType> fieldDocValueTypes;
    public final Map<String, FieldType> collectionFieldDocValueTypes;

    public final Map<String, FieldType> fieldTypes;
    public final Map<String, FieldType[]> collectionFieldTypes;
    public final Set<String> nestedFields;
    public final Map<String, Type> types;
    public final Map<String, ColumnDefinition> clusteringKeysIndexed;
    public final Map<String, ColumnDefinition> partitionKeysIndexed;
    public final Set<String> indexedColumnNames;
    public final Analyzer analyzer;
    public final String defaultField;

    public Options(Properties primary, Map<String, NumericConfig> numericFieldOptions,
                   Map<String, FieldType> fieldDocValueTypes, Map<String, FieldType> collectionFieldDocValueTypes,
                   Map<String, FieldType> fieldTypes, Map<String, FieldType[]> collectionFieldTypes,
                   Map<String, Type> types, Set<String> nestedFields, Map<String, ColumnDefinition> clusteringKeysIndexed,
                   Map<String, ColumnDefinition> partitionKeysIndexed,
                   Set<String> indexedColumnNames, Analyzer analyzer, String defaultField) {
        this.primary = primary;
        this.fields = primary.getFields();
        this.numericFieldOptions = numericFieldOptions;
        this.fieldDocValueTypes = fieldDocValueTypes;
        this.collectionFieldDocValueTypes = collectionFieldDocValueTypes;
        this.fieldTypes = fieldTypes;
        this.collectionFieldTypes = collectionFieldTypes;
        this.nestedFields = nestedFields;
        this.types = types;
        this.clusteringKeysIndexed = clusteringKeysIndexed;
        this.partitionKeysIndexed = partitionKeysIndexed;
        this.indexedColumnNames = indexedColumnNames;
        this.analyzer = analyzer;
        this.defaultField = defaultField;
    }

    public String describeAsJson() throws IOException {
        return inputMapper.writeValueAsString(this);
    }


    public Properties getProperties(String fieldName) {
        if (fieldName.contains(".")) {
            Iterable<String> parts = Splitter.on('.').splitToList(fieldName);
            return getProps(primary, parts);
        }
        return fields.get(fieldName);
    }

    public static Properties getProps(Properties rootMapping, Iterable<String> fieldName) {
        Iterator<String> parts = fieldName.iterator();
        //init current as primary field properties
        if (rootMapping == null) return null;
        Properties props = rootMapping;

        while (parts.hasNext() && props != null) {
            //as we go down the tree
            String key = parts.next();
            //find if the current key has properties associated
            props = props.getFields().get(key);
        }
        //return the props if found or null
        return props;
    }

    public boolean isObject(String fieldName) {
        Properties props = fields.get(fieldName);
        return props != null && Type.object.equals(props.getType());
    }

    public boolean shouldIndex(String fieldName) {
        return fieldTypes.containsKey(fieldName)
                || fieldDocValueTypes.containsKey(fieldName)
                || collectionFieldTypes.containsKey(fieldName)
                || collectionFieldDocValueTypes.containsKey(fieldName);
    }


    public boolean containsDocValues() {
        return !(fieldDocValueTypes.isEmpty() && collectionFieldDocValueTypes.isEmpty());
    }
}
