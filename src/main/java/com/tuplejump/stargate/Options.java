package com.tuplejump.stargate;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;
import com.tuplejump.stargate.cas.CassandraUtils;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    public static Map<String, String> getForColumn(String json, String ksName, String cfName, String colName, String idxName) {
        Map<String, String> defaultOptions = defaultOpts(null);
        JsonRootNode val = null;
        try {
            if (json != null)
                val = JDOM_PARSER.parse(json);

        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }

        Map<String, String> opts = getFieldOptions(val);
        if (opts == null || opts.isEmpty()) {
            //will ensure at least one field for indexing.
            logger.debug(String.format("No Options found for %s of %s in %s of %s", idxName, colName, cfName, ksName));
            return defaultOptions;
        }
        //just read the first row
        defaultOptions.putAll(opts);
        logger.debug(String.format("Options for %s of %s in %s of %s", idxName, colName, cfName, ksName), defaultOptions);
        return defaultOptions;

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

        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        Map<String, String> primaryFieldOpts = getFieldOptions(val);

        if (primaryFieldOpts == null || primaryFieldOpts.isEmpty()) {
            //will ensure at least one field for indexing.
            options.put(colName, defaultOpts(EMPTY_MAP));
        } else {
            options.put(colName, defaultOpts(primaryFieldOpts));
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
            options.put(field, idxOptions);
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
