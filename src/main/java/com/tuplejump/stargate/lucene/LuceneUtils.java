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

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * User: satya
 */
public class LuceneUtils {
    public static final String RK_NAME_INDEXED = "_row_key_";
    public static final String PK_NAME_STORED = "_p_key";
    public static final String PK_NAME_DOC_VAL = "_p_key_val";
    public static final String CF_TS_DOC_VAL = "_cf_ts_val";
    public static final String CF_TS_INDEXED = "_cf_ts";
    private static final Logger logger = LoggerFactory.getLogger(LuceneUtils.class);
    //  NumberFormat instances are not thread safe
    public static final ThreadLocal<NumberFormat> numberFormatThreadLocal =
            new ThreadLocal<NumberFormat>() {
                @Override
                public NumberFormat initialValue() {
                    NumberFormat fmt = NumberFormat.getInstance();
                    fmt.setGroupingUsed(false);
                    fmt.setMinimumIntegerDigits(4);
                    return fmt;
                }
            };

    public static NumericConfig numericConfig(FieldType fieldType) {
        if (fieldType.numericType() != null) {
            NumericConfig numConfig = new NumericConfigTL(fieldType.numericPrecisionStep(), fieldType.numericType());
            return numConfig;
        }
        return null;
    }

    public static File getDirectory(String ksName, String cfName, String indexName, String vNodeName) throws IOException {
        String fileName = indexName;
        String dirName = Options.defaultIndexesDir;
        dirName = dirName + File.separator + ksName + File.separator + cfName + File.separator + vNodeName;
        logger.debug("SGIndex - INDEX_FILE_NAME -" + fileName);
        logger.debug("SGIndex - INDEX_DIR_NAME -" + dirName);
        //will only create parent if not existing.
        return new File(dirName, fileName);
    }

    public static FieldType docValueTypeFrom(FieldType fieldType) {
        FieldType docValType = new FieldType(fieldType);
        if (fieldType.numericType() != null) docValType.setDocValueType(FieldInfo.DocValuesType.NUMERIC);
        else docValType.setDocValueType(FieldInfo.DocValuesType.BINARY);
        return docValType;
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

    /**
     * Deletes all files and subdirectories under "dir".
     *
     * @param dir Directory to be deleted
     * @throws java.io.IOException if any part of the tree cannot be deleted
     */
    public static void deleteRecursive(File dir) throws IOException {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children)
                deleteRecursive(new File(dir, child));
        }

        // The directory is now empty so now it can be smoked
        deleteWithConfirm(dir);
    }

    public static void deleteWithConfirm(File file) throws IOException {
        assert file.exists() : "attempted to delete non-existing file " + file.getName();
        if (logger.isDebugEnabled())
            logger.debug("Deleting " + file.getName());
        Files.delete(file.toPath());
    }

    public static Number numericDocValue(NumericDocValues rowKeyValues, int docId, Properties.Type type) throws IOException {
        Long ref = rowKeyValues == null ? 0l : rowKeyValues.get(docId);
        if (ref == null) ref = 0l;
        if (type == Properties.Type.integer) {
            return ref.intValue();
        } else if (type == Properties.Type.bigint) {
            return ref;
        } else if (type == Properties.Type.decimal) {
            return Float.intBitsToFloat(ref.intValue());
        } else if (type == Properties.Type.bigdecimal) {
            return Double.longBitsToDouble(ref);
        } else throw new IllegalArgumentException(String.format("Invalid type for numeric doc values <%s>", type));
    }


    public static SortedDocValues getRKDocValues(AtomicReader atomicReader) throws IOException {
        return atomicReader.getSortedDocValues(LuceneUtils.PK_NAME_DOC_VAL);
    }

    public static SortedDocValues getPKDocValues(AtomicReader atomicReader) throws IOException {
        return atomicReader.getSortedDocValues(LuceneUtils.PK_NAME_STORED);
    }

    public static NumericDocValues getTSDocValues(AtomicReader atomicReader) throws IOException {
        return atomicReader.getNumericDocValues(LuceneUtils.CF_TS_DOC_VAL);
    }

    public static ByteBuffer byteBufferDocValue(BinaryDocValues rowKeyValues, int docId) throws IOException {
        BytesRef ref = new BytesRef();
        rowKeyValues.get(docId, ref);
        return ByteBuffer.wrap(ref.bytes, ref.offset, ref.length);
    }

    public static String stringDocValue(BinaryDocValues rowKeyValues, int docId) throws IOException {
        BytesRef ref = new BytesRef();
        rowKeyValues.get(docId, ref);
        return ref.utf8ToString();
    }

    public static String primaryKeyName(BinaryDocValues primaryKeyNames, int docId) throws IOException {
        BytesRef ref = new BytesRef();
        primaryKeyNames.get(docId, ref);
        return new String(ref.bytes, ref.offset, ref.length, StandardCharsets.UTF_8);
    }

    public static Field idDocValue(final ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        return new SortedDocValuesField(PK_NAME_DOC_VAL, bytesRef);
    }

    public static Field pkNameDocValue(final String pkName) {
        BytesRef bytesRef = new BytesRef(pkName.getBytes(StandardCharsets.UTF_8));
        return new SortedDocValuesField(PK_NAME_STORED, bytesRef) {
            @Override
            public String toString() {
                return String.format("PK Name String->BinaryDocValuesField<%s>", pkName);
            }
        };
    }

    public static Field rowKeyIndexed(String rkValue) {
        return new StringField(RK_NAME_INDEXED, rkValue, Field.Store.NO);
    }

    public static Field textField(String name, String value) {
        return new TextField(name, value, Field.Store.NO);
    }

    public static Field stringField(String name, String value) {
        return new StringField(name, value, Field.Store.NO);
    }

    public static Field doubleField(String name, String value) {
        return new DoubleField(name, Double.parseDouble(value), Field.Store.NO);
    }

    public static Field longField(String name, String value) {
        return new LongField(name, Long.parseLong(value), Field.Store.NO);
    }

    public static Field tsField(long timestamp, FieldType fieldType) {
        return new LongField(CF_TS_INDEXED, timestamp, fieldType);
    }

    public static Field tsDocValues(final long timestamp) {
        return new NumericDocValuesField(CF_TS_DOC_VAL, timestamp) {
            @Override
            public String toString() {
                return String.format("Timestamp NumericDocValuesField<%s>", timestamp);
            }
        };
    }

    public static Term idTerm(String pkString) {
        return new Term(PK_NAME_STORED, pkString);
    }

    public static Term rkTerm(String rkString) {
        return new Term(RK_NAME_INDEXED, rkString);
    }

    public static Term tsTerm(long ts) {
        BytesRef tsBytes = new BytesRef();
        NumericUtils.longToPrefixCodedBytes(ts, NumericUtils.PRECISION_STEP_DEFAULT, tsBytes);
        return new Term(CF_TS_INDEXED, tsBytes);
    }

    public static Field field(String name, Properties properties, String value, FieldType fieldType) {
        Properties.Type type = properties.getType();
        if (type == Properties.Type.integer) {
            return new IntField(name, Integer.parseInt(value), fieldType);
        } else if (type == Properties.Type.bigint) {
            return new LongField(name, Long.parseLong(value), fieldType);
        } else if (type == Properties.Type.bigdecimal) {
            return new DoubleField(name, Double.parseDouble(value), fieldType);
        } else if (type == Properties.Type.decimal) {
            return new FloatField(name, Float.parseFloat(value), fieldType);
        } else if (type == Properties.Type.date) {
            //TODO - set correct locale
            FormatDateTimeFormatter formatter = Dates.forPattern(value, Locale.US);
            return new LongField(name, formatter.parser().parseMillis(value), fieldType);
        } else if (type == Properties.Type.bool) {
            Boolean val = Boolean.parseBoolean(value);
            return new Field(name, val.toString(), fieldType);
        } else {
            return new Field(name, value, fieldType);
        }
    }

    public static class NumericConfigTL extends NumericConfig {

        static NumberFormat dummyInstance = NumberFormat.getInstance();

        public NumericConfigTL(int precisionStep, FieldType.NumericType type) {
            super(precisionStep, dummyInstance, type);
        }

        @Override
        public NumberFormat getNumberFormat() {
            return numberFormatThreadLocal.get();
        }
    }
}
