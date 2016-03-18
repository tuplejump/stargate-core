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

package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.lucene.LuceneUtils;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.lucene.Type;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.service.StorageService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;

import java.io.IOException;
import java.util.*;

/**
 * Utilities to read Cassandra configuration
 */
public class CassandraUtils {
    public static final Token MINIMUM_TOKEN = StorageService.getPartitioner().getMinimumToken();
    public static final Long MINIMUM_TOKEN_VALUE = (Long) MINIMUM_TOKEN.getTokenValue();

    public static String[] getDataDirs() throws IOException, ConfigurationException {
        return DatabaseDescriptor.getAllDataFileLocations();
    }

    public static Options getOptions(String columnName, ColumnFamilyStore baseCfs, String json) {
        try {
            Properties mapping = Options.inputMapper.readValue(json, Properties.class);
            return getOptions(mapping, baseCfs, columnName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isMinimumToken(Token token) {
        return MINIMUM_TOKEN.compareTo(token) == 0;
    }

    public static Options getOptions(Properties mapping, ColumnFamilyStore baseCfs, String colName) {
        Map<String, NumericConfig> numericFieldOptions = new HashMap<>();
        Map<String, FieldType> fieldDocValueTypes = new TreeMap<>();
        Map<String, FieldType> collectionFieldDocValueTypes = new TreeMap<>();

        Map<String, FieldType> fieldTypes = new TreeMap<>();
        Map<String, FieldType[]> collectionFieldTypes = new TreeMap<>();
        Map<String, ColumnDefinition> validators = new TreeMap<>();
        Map<String, ColumnDefinition> clusteringKeysIndexed = new LinkedHashMap<>();
        Map<String, ColumnDefinition> partitionKeysIndexed = new LinkedHashMap<>();
        Set<String> indexedColumnNames;


        //getForRow all the fields options.
        indexedColumnNames = new TreeSet<>();
        indexedColumnNames.addAll(mapping.getFields().keySet());

        Set<String> added = new HashSet<>(indexedColumnNames.size());
        List<ColumnDefinition> partitionKeys = baseCfs.metadata.partitionKeyColumns();
        List<ColumnDefinition> clusteringKeys = baseCfs.metadata.clusteringColumns();

        for (ColumnDefinition colDef : partitionKeys) {
            String columnName = colDef.name.toString();
            if (Options.logger.isDebugEnabled()) {
                Options.logger.debug("Partition key name is {} and index is {}", colName, colDef.position());
            }
            validators.put(columnName, colDef);
            if (indexedColumnNames.contains(columnName)) {
                partitionKeysIndexed.put(colName, colDef);
                addPropertiesAndFieldType(mapping, numericFieldOptions, fieldDocValueTypes, collectionFieldDocValueTypes, fieldTypes, collectionFieldTypes, added, colDef, columnName);
            }
        }


        for (ColumnDefinition colDef : clusteringKeys) {
            String columnName = colDef.name.toString();
            if (Options.logger.isDebugEnabled()) {
                Options.logger.debug("Clustering key name is {} and index is {}", colName, colDef.position() + 1);
            }
            validators.put(columnName, colDef);
            if (indexedColumnNames.contains(columnName)) {
                clusteringKeysIndexed.put(columnName, colDef);
                addPropertiesAndFieldType(mapping, numericFieldOptions, fieldDocValueTypes, collectionFieldDocValueTypes, fieldTypes, collectionFieldTypes, added, colDef, columnName);
            }
        }

        for (String columnName : indexedColumnNames) {
            if (added.add(columnName.toLowerCase())) {
                Properties options = mapping.getFields().get(columnName);
                ColumnDefinition colDef = getColumnDefinition(baseCfs, columnName);
                if (colDef != null) {
                    validators.put(columnName, colDef);
                    addFieldType(columnName, colDef.type, options, numericFieldOptions, fieldDocValueTypes, collectionFieldDocValueTypes, fieldTypes, collectionFieldTypes);
                } else {
                    throw new IllegalArgumentException(String.format("Column Definition for %s not found", columnName));
                }
                if (options.getType() == Type.object) {
                    mapping.getFields().putAll(options.getFields());
                }
            }

        }
        Set<ColumnDefinition> otherColumns = baseCfs.metadata.regularColumns();
        for (ColumnDefinition colDef : otherColumns) {
            String columnName = UTF8Type.instance.getString(colDef.name.bytes);
            validators.put(columnName, colDef);
        }

        numericFieldOptions.putAll(mapping.getDynamicNumericConfig());

        Analyzer defaultAnalyzer = mapping.getLuceneAnalyzer();
        Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, mapping.perFieldAnalyzers());
        Map<String, Type> types = new TreeMap<>();
        Set<String> nestedFields = new TreeSet<>();
        for (Map.Entry<String, ColumnDefinition> entry : validators.entrySet()) {
            CQL3Type cql3Type = entry.getValue().type.asCQL3Type();
            AbstractType inner = getValueValidator(cql3Type.getType());
            if (cql3Type.isCollection()) {
                types.put(entry.getKey(), fromAbstractType(inner.asCQL3Type()));
                nestedFields.add(entry.getKey());
            } else {
                types.put(entry.getKey(), fromAbstractType(cql3Type));
            }

        }

        return new Options(mapping, numericFieldOptions,
                fieldDocValueTypes, collectionFieldDocValueTypes,
                fieldTypes, collectionFieldTypes, types,
                nestedFields, clusteringKeysIndexed, partitionKeysIndexed,
                indexedColumnNames, analyzer, colName);
    }

    private static void addPropertiesAndFieldType(Properties mapping, Map<String, NumericConfig> numericFieldOptions, Map<String, FieldType> fieldDocValueTypes, Map<String, FieldType> collectionFieldDocValueTypes, Map<String, FieldType> fieldTypes, Map<String, FieldType[]> collectionFieldTypes, Set<String> added, ColumnDefinition colDef, String columnName) {
        Properties properties = mapping.getFields().get(columnName.toLowerCase());
        addFieldType(columnName, colDef.type, properties, numericFieldOptions, fieldDocValueTypes, collectionFieldDocValueTypes, fieldTypes, collectionFieldTypes);
        added.add(columnName.toLowerCase());
    }

    private static ColumnDefinition getColumnDefinition(ColumnFamilyStore baseCfs, String columnName) {
        Iterable<ColumnDefinition> cols = baseCfs.metadata.regularAndStaticColumns();
        for (ColumnDefinition columnDefinition : cols) {
            if (columnDefinition.name.toString().equalsIgnoreCase(columnName)) return columnDefinition;
        }
        return null;
    }

    private static void addFieldType(String columnName, AbstractType validator, Properties properties,
                                     Map<String, NumericConfig> numericFieldOptions,
                                     Map<String, FieldType> fieldDocValueTypes, Map<String, FieldType> collectionFieldDocValueTypes,
                                     Map<String, FieldType> fieldTypes, Map<String, FieldType[]> collectionFieldTypes) {

        if (validator.isCollection()) {
            if (validator instanceof MapType) {
                properties.setType(Type.map);
                MapType mapType = (MapType) validator;
                AbstractType keyValidator = mapType.getKeysType();
                AbstractType valueValidator = mapType.getValuesType();
                Properties keyProps = properties.getFields().get("_key");
                Properties valueProps = properties.getFields().get("_value");
                if (keyProps == null) {
                    keyProps = new Properties();
                    keyProps.setAnalyzer(properties.getAnalyzer());
                    properties.getFields().put("_key", keyProps);
                }
                if (valueProps == null) {
                    valueProps = new Properties();
                    valueProps.setAnalyzer(properties.getAnalyzer());
                    properties.getFields().put("_value", valueProps);
                }
                setFromAbstractType(keyProps, keyValidator);
                setFromAbstractType(valueProps, valueValidator);
                FieldType keyFieldType = fieldType(keyProps, keyValidator);
                FieldType valueFieldType = fieldType(valueProps, valueValidator);

                if (valueProps.getStriped() == Properties.Striped.only || valueProps.getStriped() == Properties.Striped.also) {
                    FieldType docValueType = LuceneUtils.docValueTypeFrom(valueFieldType);
                    collectionFieldDocValueTypes.put(columnName, docValueType);
                }
                if (!(valueProps.getStriped() == Properties.Striped.only))
                    collectionFieldTypes.put(columnName, new FieldType[]{keyFieldType, valueFieldType});

            } else if (validator instanceof ListType || validator instanceof SetType) {
                AbstractType elementValidator;
                if (validator instanceof SetType) {
                    SetType setType = (SetType) validator;
                    elementValidator = setType.getElementsType();
                } else {
                    ListType listType = (ListType) validator;
                    elementValidator = listType.getElementsType();
                }
                setFromAbstractType(properties, elementValidator);

                FieldType elementFieldType = fieldType(properties, elementValidator);
                if (properties.getStriped() == Properties.Striped.only || properties.getStriped() == Properties.Striped.also) {
                    FieldType docValueType = LuceneUtils.docValueTypeFrom(elementFieldType);
                    collectionFieldDocValueTypes.put(columnName, docValueType);
                }
                if (!(properties.getStriped() == Properties.Striped.only))
                    collectionFieldTypes.put(columnName, new FieldType[]{elementFieldType});
            }

        } else {
            setFromAbstractType(properties, validator);
            FieldType fieldType = fieldType(properties, validator);
            if (fieldType.numericType() != null) {
                numericFieldOptions.put(columnName, LuceneUtils.numericConfig(fieldType));
            }
            if (properties.getStriped() == Properties.Striped.only || properties.getStriped() == Properties.Striped.also) {
                FieldType docValueType = LuceneUtils.docValueTypeFrom(fieldType);
                fieldDocValueTypes.put(columnName, docValueType);
            }
            if (properties.getStriped() != Properties.Striped.only)
                fieldTypes.put(columnName, fieldType);
        }
    }

    public static void setFromAbstractType(Properties properties, AbstractType type) {
        if (properties.getType() != null) return;
        CQL3Type cqlType = type.asCQL3Type();
        Type fromAbstractType = fromAbstractType(cqlType);
        properties.setType(fromAbstractType);

    }

    public static Type fromAbstractType(CQL3Type cqlType) {
        Type fromAbstractType;
        if (cqlType == CQL3Type.Native.INT) {
            fromAbstractType = Type.integer;
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            fromAbstractType = Type.bigint;
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            fromAbstractType = Type.bigdecimal;
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            fromAbstractType = Type.decimal;
        } else if (cqlType == CQL3Type.Native.TEXT || cqlType == CQL3Type.Native.ASCII) {
            fromAbstractType = Type.text;
        } else if (cqlType == CQL3Type.Native.VARCHAR) {
            fromAbstractType = Type.string;
        } else if (cqlType == CQL3Type.Native.UUID) {
            fromAbstractType = Type.uuid;
        } else if (cqlType == CQL3Type.Native.TIMEUUID) {
            //TimeUUID toString and reorder to make it comparable.
            fromAbstractType = Type.timeuuid;
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            fromAbstractType = Type.date;
        } else if (cqlType == CQL3Type.Native.BOOLEAN) {
            fromAbstractType = Type.bool;
        } else {
            fromAbstractType = Type.text;
        }
        return fromAbstractType;
    }

    public static FieldType fieldType(Properties properties, AbstractType validator) {
        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(properties.getIndexOptions());
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

    public static AbstractType getValueValidator(AbstractType abstractType) {
        if (abstractType instanceof CollectionType) {
            if (abstractType instanceof MapType) {
                MapType mapType = (MapType) abstractType;
                return mapType.valueComparator();
            } else if (abstractType instanceof SetType) {
                SetType setType = (SetType) abstractType;
                return setType.nameComparator();
            } else if (abstractType instanceof ListType) {
                ListType listType = (ListType) abstractType;
                return listType.valueComparator();
            }
        }
        return abstractType;
    }


}