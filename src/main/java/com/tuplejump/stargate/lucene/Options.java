package com.tuplejump.stargate.lucene;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;
import com.tuplejump.stargate.Constants;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.cassandra.CassandraUtils;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

import static com.tuplejump.stargate.Constants.*;
import static java.util.Collections.EMPTY_MAP;

/**
 * User: satya
 * <p/>
 * This is used to get index options and field options to apply to the lucene based cassandra secondary indexes.
 */
public class Options {
    private static final Logger logger = LoggerFactory.getLogger(Options.class);

    private static Map<String, String> idFieldOptions = new HashMap<>();
    private static String defaultIndexesDir;
    private static final JdomParser JDOM_PARSER = new JdomParser();

    public final Map<String, Map<String, String>> fieldOptions;
    public final Map<String, NumericConfig> numericFieldOptions;
    public final Map<String, FieldType> fieldTypes;
    public final Map<String, AbstractType> validators;
    public final Map<String, String> primaryFieldOptions;
    public final Map<Integer, Pair<String, ByteBuffer>> clusteringKeysIndexed;
    public final Set<String> indexedColumnNames;
    public final Version luceneVersion;
    public final Analyzer analyzer;
    public final String defaultField;

    public Options(String defaultField, Set<String> indexedColumnNames, Map<Integer, Pair<String, ByteBuffer>> clusteringKeysIndexed, Map<String, AbstractType> validators, Map<String, Map<String, String>> fieldOptions, Map<String, NumericConfig> numericFieldOptions, Map<String, FieldType> fieldTypes, Map<String, String> primaryFieldOptions) {
        this.defaultField = defaultField;
        this.indexedColumnNames = indexedColumnNames;
        this.fieldOptions = fieldOptions;
        this.numericFieldOptions = numericFieldOptions;
        this.fieldTypes = fieldTypes;
        this.validators = validators;
        this.primaryFieldOptions = primaryFieldOptions;
        this.clusteringKeysIndexed = clusteringKeysIndexed;
        String versionStr = primaryFieldOptions.get(LUCENE_VERSION);
        this.luceneVersion = Version.parseLeniently(versionStr);
        this.analyzer = getAnalyzer(primaryFieldOptions, fieldOptions, luceneVersion);
    }

    private Analyzer getAnalyzer(Map<String, String> options, Map<String, Map<String, String>> perFieldOptions, Version luceneV) {
        Analyzer retVal;
        Analyzer defaultAnalyzer = AnalyzerFactory.getAnalyzer(options, luceneV);
        if (perFieldOptions == null) {
            retVal = defaultAnalyzer;
        } else {
            Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> fieldOptions : perFieldOptions.entrySet()) {
                perFieldAnalyzers.put(fieldOptions.getKey(), AnalyzerFactory.getAnalyzer(fieldOptions.getValue(), luceneV));
            }
            retVal = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
        }
        return retVal;
    }

    public static Options makeOptions(ColumnFamilyStore baseCfs, ColumnDefinition columnDef, String colName) {
        String optionsJson = columnDef.getIndexOptions().get(Constants.INDEX_OPTIONS_JSON);
        //getForRow all the fields options.
        Map<String, Map<String, String>> fieldOptions = Options.getForRow(optionsJson, colName);
        if (logger.isDebugEnabled())
            logger.debug("SGIndex field options -" + fieldOptions);
        Set<String> stringColumnNames = new TreeSet<>();
        stringColumnNames.addAll(fieldOptions.keySet());

        Map<Integer, Pair<String, ByteBuffer>> clusteringKeysIndexed = new LinkedHashMap<>();
        Set<String> added = new HashSet<>(stringColumnNames.size());
        List<ColumnDefinition> clusteringKeys = baseCfs.metadata.clusteringKeyColumns();
        Map<String, FieldType> fieldTypes = new TreeMap<>();
        Map<String, AbstractType> validators = new TreeMap<>();
        Map<String, NumericConfig> numericConfigMap = new HashMap<>();
        for (ColumnDefinition colDef : clusteringKeys) {
            String columnName = CFDefinition.definitionType.getString(colDef.name);
            if (logger.isDebugEnabled()) {
                logger.debug("Clustering key name is {} and index is {}", colName, colDef.componentIndex + 1);
            }
            if (stringColumnNames.contains(columnName)) {
                clusteringKeysIndexed.put(colDef.componentIndex + 1, Pair.create(columnName, colDef.name));
                addFieldType(baseCfs.name, columnName, colDef.getValidator(), numericConfigMap, fieldOptions, fieldTypes, validators);
                added.add(columnName);
            }
        }

        for (String columnName : stringColumnNames) {
            if (added.add(columnName.toLowerCase())) {
                ColumnDefinition colDef = getColumnDefinition(baseCfs, columnName);
                if (colDef == null)
                    throw new IllegalArgumentException(String.format("Column Definition for %s not found", columnName));
                addFieldType(baseCfs.name, columnName, colDef.getValidator(), numericConfigMap, fieldOptions, fieldTypes, validators);
            }
        }

        Map<String, String> idxOptions = fieldOptions.get(colName);
        if (logger.isDebugEnabled()) {
            logger.debug("SGIndex index options -" + idxOptions);
            logger.debug("SGIndex Column names being indexed -" + stringColumnNames);
        }

        return new Options(colName, stringColumnNames, clusteringKeysIndexed, validators, fieldOptions, numericConfigMap, fieldTypes, idxOptions);
    }

    private static ColumnDefinition getColumnDefinition(ColumnFamilyStore baseCfs, String columnName) {
        Iterable<ColumnDefinition> cols = baseCfs.metadata.regularAndStaticColumns();
        for (ColumnDefinition columnDefinition : cols) {
            String fromColDef = CFDefinition.definitionType.getString(columnDefinition.name);
            if (fromColDef.equalsIgnoreCase(columnName)) return columnDefinition;
        }
        return null;
    }

    private static void addFieldType(String cfName, String columnName, AbstractType validator, Map<String, NumericConfig> numericConfigMap, Map<String, Map<String, String>> fieldOptions, Map<String, FieldType> fieldTypes, Map<String, AbstractType> validators) {
        Map<String, String> options = fieldOptions.get(columnName);
        validators.put(columnName, validator);
        FieldType fieldType = Utils.fieldType(options, cfName, columnName, validator);
        if (fieldType.numericType() != null) {
            numericConfigMap.put(columnName, Utils.numericConfig(options, fieldType));
        }
        fieldTypes.put(columnName, fieldType);
    }


    static {
        idFieldOptions.put(tokenized, "false");
        //need searching while deleting
        idFieldOptions.put(indexed, "true");
        defaultIndexesDir = System.getProperty("sg.index.dir", "_DUMMY_");
        if (defaultIndexesDir.equals("_DUMMY_")) {
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

    private static Map<String, String> getFieldOptions(JsonNode val) {
        if (val != null) {
            Map<JsonStringNode, JsonNode> fields = val.getFields();
            Map<String, String> opts = new HashMap<>();
            for (Map.Entry<JsonStringNode, JsonNode> fieldEntry : fields.entrySet()) {
                String keyStr = fieldEntry.getKey().getText();
                JsonNode value = fieldEntry.getValue();
                if (value instanceof JsonStringNode) {
                    opts.put(keyStr, value.getText());
                }
            }
            return opts;
        }
        return null;
    }


    public static Map<String, Map<String, String>> getForRow(String json, String colName) {
        Map<String, Map<String, String>> options = new HashMap<>();
        JsonRootNode val = null;
        try {
            if (json != null)
                val = JDOM_PARSER.parse(json);
            else
                val = JDOM_PARSER.parse("{\"fields\":[]}");
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        Map<String, String> primaryFieldOpts = getFieldOptions(val);

        if (primaryFieldOpts == null || primaryFieldOpts.isEmpty()) {
            //will ensure at least one field for indexing.
            options.put(colName.toLowerCase(), defaultOpts(EMPTY_MAP));
        } else {
            options.put(colName.toLowerCase(), defaultOpts(primaryFieldOpts));
        }
        assert val != null;
        List<JsonNode> fieldsArr = val.getArrayNode(FIELDS);
        Set<String> fields = new HashSet<>();
        for (JsonNode fieldNode : fieldsArr) {
            fields.add(fieldNode.getText());
        }
        //now read through the fields and get options for each of them.
        for (String field : fields) {
            Map<String, String> idxOptions = defaultOpts(primaryFieldOpts);
            JsonNode fieldNode;
            try {
                fieldNode = val.getNode(field);

            } catch (Exception e) {
                fieldNode = null;
            }
            //default options are overridden
            Map<String, String> fieldOpts = getFieldOptions(fieldNode);
            if (fieldOpts != null)
                idxOptions.putAll(fieldOpts);
            options.put(field.toLowerCase(), idxOptions);
        }

        return options;
    }


    public static Map<String, String> defaultOpts(Map<String, String> overrides) {
        Map<String, String> defaultOpts = new HashMap<>();
        defaultOpts.put(indexed, "true");
        defaultOpts.put(tokenized, "true");
        defaultOpts.put(stored, "false");
        defaultOpts.put(storeTermVectors, "false");
        defaultOpts.put(storeTermVectorOffsets, "false");
        defaultOpts.put(storeTermVectorPayloads, "false");
        defaultOpts.put(storeTermVectorPositions, "false");
        defaultOpts.put(omitNorms, "true");
        defaultOpts.put(ANALYZER, Analyzers.KeywordAnalyzer.toString());
        defaultOpts.put(IDXW_MAX_FL, "UNLIMITED");
        defaultOpts.put(LUCENE_VERSION, Version.LUCENE_47.name());
        defaultOpts.put(INDEX_DIR_NAME, defaultIndexesDir);
        if (overrides != null && !overrides.isEmpty())
            defaultOpts.putAll(overrides);
        return defaultOpts;
    }

    public static Map<String, String> idFieldOptions() {
        return defaultOpts(idFieldOptions);
    }

}
