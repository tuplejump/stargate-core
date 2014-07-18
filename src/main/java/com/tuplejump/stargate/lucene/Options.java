package com.tuplejump.stargate.lucene;

import com.google.common.base.Splitter;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.cassandra.CassandraUtils;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 * <p/>
 * This is used to get index options and field options to apply to the lucene based cassandra secondary indexes.
 */
public class Options {
    private static final Logger logger = LoggerFactory.getLogger(Options.class);
    public static final String DUMMY_DIR = "_DUMMY_";
    public static String defaultIndexesDir = System.getProperty("sg.index.dir", DUMMY_DIR);
    public static final ObjectMapper jsonMapper = new ObjectMapper();


    static {
        jsonMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        jsonMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        jsonMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
        SimpleModule module = new SimpleModule("LowerCaseKeyDeserializer",
                new org.codehaus.jackson.Version(1, 9, 0, null));
        module.addKeyDeserializer(Object.class, new LowerCaseKeyDeserializer());
        module.addKeyDeserializer(Map.class, new LowerCaseKeyDeserializer());
        jsonMapper.registerModule(module);


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

    static class LowerCaseKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctx)
                throws IOException {
            return key.toLowerCase();
        }
    }


    final Map<String, Properties> fields;
    public final Properties primary;
    public final Map<String, NumericConfig> numericFieldOptions;
    public final Map<String, FieldType> fieldTypes;
    public final Map<String, FieldType[]> collectionFieldTypes;
    public final Map<String, AbstractType> validators;
    public final Map<Integer, Pair<String, ByteBuffer>> clusteringKeysIndexed;
    public final Map<String, Analyzer> perFieldAnalyzers;
    public final Set<String> indexedColumnNames;
    public final Analyzer analyzer;
    public final String defaultField;


    public Properties getProperties(String fieldName) {
        if (fieldName.contains(".")) {
            Iterable<String> parts = Splitter.on('.').splitToList(fieldName);
            return getProps(primary, parts);
        }
        return fields.get(fieldName);
    }

    public Map<String, Properties> getFields() {
        return fields;
    }


    public boolean isObject(String fieldName) {
        Properties props = fields.get(fieldName);
        if (props != null) return Properties.Type.object.equals(props.getType());
        return false;
    }

    public boolean shouldIndex(String fieldName) {
        if (fieldTypes.containsKey(fieldName)) return true;
        if (collectionFieldTypes.containsKey(fieldName)) return true;
        return false;
    }


    public static Options getOptions(String columnName, ColumnFamilyStore baseCfs, String json) {
        try {
            Properties mapping = jsonMapper.readValue(json, Properties.class);
            return new Options(mapping, baseCfs, columnName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Options(Properties mapping, ColumnFamilyStore baseCfs, String colName) {
        this.primary = mapping;
        this.fields = mapping.getFields();
        //getForRow all the fields options.
        indexedColumnNames = new TreeSet<>();
        indexedColumnNames.addAll(mapping.getFields().keySet());

        clusteringKeysIndexed = new LinkedHashMap<>();
        Set<String> added = new HashSet<>(indexedColumnNames.size());
        List<ColumnDefinition> clusteringKeys = baseCfs.metadata.clusteringKeyColumns();
        fieldTypes = new TreeMap<>();
        validators = new TreeMap<>();
        collectionFieldTypes = new TreeMap<>();
        numericFieldOptions = new HashMap<>();
        for (ColumnDefinition colDef : clusteringKeys) {
            String columnName = CFDefinition.definitionType.getString(colDef.name);
            if (logger.isDebugEnabled()) {
                logger.debug("Clustering key name is {} and index is {}", colName, colDef.componentIndex + 1);
            }
            if (indexedColumnNames.contains(columnName)) {
                clusteringKeysIndexed.put(colDef.componentIndex + 1, Pair.create(columnName, colDef.name));
                validators.put(columnName, colDef.getValidator());
                Properties properties = mapping.getFields().get(columnName.toLowerCase());
                addFieldType(columnName, colDef.getValidator(), numericFieldOptions, properties, fieldTypes, collectionFieldTypes);
                added.add(columnName.toLowerCase());
            }
        }

        for (String columnName : indexedColumnNames) {
            if (added.add(columnName.toLowerCase())) {
                Properties options = mapping.getFields().get(columnName);
                ColumnDefinition colDef = getColumnDefinition(baseCfs, columnName);
                if (options.getType() == Properties.Type.object) {
                    mapping.fields.putAll(options.fields);
                }
                if (colDef != null) {
                    validators.put(columnName, colDef.getValidator());
                    addFieldType(columnName, colDef.getValidator(), numericFieldOptions, options, fieldTypes, collectionFieldTypes);
                } else {
                    throw new IllegalArgumentException(String.format("Column Definition for %s not found", columnName));
                }
            }
        }
        numericFieldOptions.putAll(primary.getDynamicNumericConfig());
        this.defaultField = colName;
        Analyzer defaultAnalyzer = mapping.getAnalyzer();
        this.perFieldAnalyzers = mapping.perFieldAnalyzers();
        this.analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
    }

    private static ColumnDefinition getColumnDefinition(ColumnFamilyStore baseCfs, String columnName) {
        Iterable<ColumnDefinition> cols = baseCfs.metadata.regularAndStaticColumns();
        for (ColumnDefinition columnDefinition : cols) {
            String fromColDef = CFDefinition.definitionType.getString(columnDefinition.name);
            if (fromColDef.equalsIgnoreCase(columnName)) return columnDefinition;
        }
        return null;
    }

    private static void addFieldType(String columnName, AbstractType validator, Map<String, NumericConfig> numericConfigMap, Properties properties, Map<String, FieldType> fieldTypes, Map<String, FieldType[]> collectionFieldTypes) {
        if (validator.isCollection()) {
            if (validator instanceof MapType) {
                properties.setType(Properties.Type.map);
                MapType mapType = (MapType) validator;
                AbstractType keyValidator = mapType.keys;
                AbstractType valueValidator = mapType.values;
                Properties keyProps = properties.getFields().get("key");
                Properties valueProps = properties.getFields().get("value");
                if (keyProps == null) {
                    keyProps = new Properties();
                    keyProps.setAnalyzer(properties.analyzer);
                    properties.fields.put("key", keyProps);
                }
                if (valueProps == null) {
                    valueProps = new Properties();
                    valueProps.setAnalyzer(properties.analyzer);
                    properties.fields.put("value", valueProps);
                }
                keyProps.setFromAbstractType(keyValidator);
                valueProps.setFromAbstractType(valueValidator);
                FieldType keyFieldType = Properties.fieldType(keyProps, keyValidator);
                FieldType valueFieldType = Properties.fieldType(valueProps, valueValidator);
                collectionFieldTypes.put(columnName, new FieldType[]{keyFieldType, valueFieldType});
            } else if (validator instanceof SetType) {
                SetType setType = (SetType) validator;
                AbstractType elementValidator = setType.elements;
                properties.setFromAbstractType(elementValidator);
                FieldType elementFieldType = Properties.fieldType(properties, elementValidator);
                collectionFieldTypes.put(columnName, new FieldType[]{elementFieldType});
            } else if (validator instanceof ListType) {
                ListType listType = (ListType) validator;
                AbstractType elementValidator = listType.elements;
                properties.setFromAbstractType(elementValidator);
                FieldType elementFieldType = Properties.fieldType(properties, elementValidator);
                collectionFieldTypes.put(columnName, new FieldType[]{elementFieldType});
            }

        } else {
            properties.setFromAbstractType(validator);
            FieldType fieldType = Properties.fieldType(properties, validator);
            if (fieldType.numericType() != null) {
                numericConfigMap.put(columnName, Utils.numericConfig(fieldType));
            }
            fieldTypes.put(columnName, fieldType);
        }
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

}
