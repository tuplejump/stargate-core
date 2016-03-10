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

import org.apache.cassandra.dht.Token;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;

import static org.apache.lucene.search.BooleanClause.Occur.FILTER;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

/**
 * User: satya
 */
public class LuceneUtils {
    public static final String RK_INDEXED = "_rk_idx";
    public static final String PK_INDEXED = "_pk_idx";
    public static final String TOKEN_INDEXED = "_token_idx";

    public static final String RK_BYTES = "_rk_bytes";
    public static final String PK_BYTES = "_pk_bytes";
    public static final String TOKEN_LONG = "_token_val";
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
            return new NumericConfigTL(fieldType.numericPrecisionStep(), fieldType.numericType());
        }
        return null;
    }

    public static File getDirectory(String ksName, String cfName, String indexName, String vNodeName) throws IOException {
        String dirName = Options.defaultIndexesDir;
        dirName = dirName + File.separator + ksName + File.separator + cfName + File.separator + vNodeName;
        if (logger.isDebugEnabled()) {
            logger.debug("SGIndex - INDEX_FILE_NAME - {}", indexName);
            logger.debug("SGIndex - INDEX_DIR_NAME - {}", dirName);
        }
        //will only create parent if not existing.
        return new File(dirName, indexName);
    }

    public static FieldType docValueTypeFrom(FieldType fieldType) {
        FieldType docValType = new FieldType(fieldType);
        if (fieldType.numericType() != null) docValType.setDocValuesType(DocValuesType.NUMERIC);
        else docValType.setDocValuesType(DocValuesType.BINARY);
        return docValType;
    }

    public static FieldType dynamicFieldType(Properties properties) {
        FieldType fieldType = new FieldType();
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

    public static Number numericDocValue(NumericDocValues rowKeyValues, int docId, Type type) throws IOException {
        Long ref = rowKeyValues == null ? 0L : rowKeyValues.get(docId);
        if (type == Type.integer) {
            return ref.intValue();
        } else if (type == Type.bigint) {
            return ref;
        } else if (type == Type.decimal) {
            return Float.intBitsToFloat(ref.intValue());
        } else if (type == Type.bigdecimal) {
            return Double.longBitsToDouble(ref);
        } else throw new IllegalArgumentException(String.format("Invalid type for numeric doc values <%s>", type));
    }


    public static SortedDocValues getPKBytesDocValues(LeafReader atomicReader) throws IOException {
        return atomicReader.getSortedDocValues(LuceneUtils.PK_BYTES);
    }

    public static SortedDocValues getRKBytesDocValues(LeafReader atomicReader) throws IOException {
        return atomicReader.getSortedDocValues(LuceneUtils.RK_BYTES);
    }


    public static ByteBuffer byteBufferDocValue(BinaryDocValues docValues, int docId) throws IOException {
        BytesRef ref = BytesRef.deepCopyOf(docValues.get(docId));
        return ByteBuffer.wrap(ref.bytes, ref.offset, ref.length);
    }

    public static String stringDocValue(BinaryDocValues rowKeyValues, int docId) throws IOException {
        BytesRef ref = rowKeyValues.get(docId);
        return ref.utf8ToString();
    }

    public static Field pkBytesDocValue(final ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        return new SortedDocValuesField(PK_BYTES, bytesRef);
    }

    public static Field rkBytesDocValue(final ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        return new SortedDocValuesField(RK_BYTES, bytesRef);
    }

    public static Field tokenBytesDocValue(final Token token) {
        return new NumericDocValuesField(TOKEN_LONG, ((Number) token.getTokenValue()).longValue());
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


    public static Term primaryKeyTerm(String pkString) {
        return new Term(PK_INDEXED, pkString);
    }

    public static Field primaryKeyField(String pkString) {
        return new StringField(PK_INDEXED, pkString, Field.Store.NO);
    }

    public static Term rowkeyTerm(String rkString) {
        return new Term(RK_INDEXED, rkString);
    }

    public static Field rowKeyIndexed(String rkValue) {
        return new StringField(RK_INDEXED, rkValue, Field.Store.NO);
    }


    public static Term tokenTerm(Token token) {
        Long value = (Long) token.getTokenValue();
        BytesRefBuilder bytesRef = new BytesRefBuilder();
        NumericUtils.longToPrefixCoded(value, 0, bytesRef);
        return new Term(TOKEN_INDEXED, bytesRef.get());
    }

    public static Field tokenIndexed(Token token) {
        Long value = (Long) token.getTokenValue();
        return new LongField(TOKEN_INDEXED, value, Field.Store.NO);
    }

    public static Term tsTerm(long ts) {
        BytesRefBuilder tsBytes = new BytesRefBuilder();
        NumericUtils.longToPrefixCoded(ts, NumericUtils.PRECISION_STEP_DEFAULT, tsBytes);
        return new Term(CF_TS_INDEXED, tsBytes);
    }

    public static Field field(String name, Properties properties, String value, FieldType fieldType) {
        Type type = properties.getType();
        if (type == Type.integer) {
            return new IntField(name, Integer.parseInt(value), fieldType);
        } else if (type == Type.bigint) {
            return new LongField(name, Long.parseLong(value), fieldType);
        } else if (type == Type.bigdecimal) {
            return new DoubleField(name, Double.parseDouble(value), fieldType);
        } else if (type == Type.decimal) {
            return new FloatField(name, Float.parseFloat(value), fieldType);
        } else if (type == Type.date) {
            //TODO - set correct locale
            FormatDateTimeFormatter formatter = Dates.forPattern(value, Locale.US);
            return new LongField(name, formatter.parser().parseMillis(value), fieldType);
        } else if (type == Type.bool) {
            Boolean val = Boolean.parseBoolean(value);
            return new Field(name, val.toString(), fieldType);
        } else {
            return new Field(name, value, fieldType);
        }
    }

    public static Query getPKRangeDeleteQuery(String startPK, String endPK) {
        return TermRangeQuery.newStringRange(PK_INDEXED, startPK, endPK, true, true);
    }

    public static Query buildQuery(Query query, Query filter, Query range) {
        if (query == null && filter == null && range == null) {
            return new MatchAllDocsQuery();
        }
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        if (range != null) {
            builder.add(range, FILTER);
        }
        if (filter != null) {
            builder.add(filter, FILTER);
        }
        if (query != null) {
            builder.add(query, MUST);
        }
        return new CachingWrapperQuery(builder.build());
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
