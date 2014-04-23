package com.tuplejump.stargate;

import com.tuplejump.stargate.luc.BinaryTokenStream;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import static com.tuplejump.stargate.Constants.*;

/**
 * User: satya
 * <p/>
 * Utility methods to deal in fields.
 */
public class Fields {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Get the field type with options specified in the indexes meta table.
     */
    public static FieldType fieldType(Map<String, String> options, String cfName, String name, AbstractType validator) {
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(getBool(options.get(indexed)));
        fieldType.setTokenized(getBool(options.get(tokenized)));
        fieldType.setStored(getBool(options.get(stored)));
        fieldType.setStoreTermVectors(getBool(options.get(storeTermVectors)));
        fieldType.setStoreTermVectorOffsets(getBool(options.get(storeTermVectorOffsets)));
        fieldType.setStoreTermVectorPayloads(getBool(options.get(storeTermVectorPayloads)));
        fieldType.setStoreTermVectorPositions(getBool(options.get(storeTermVectorPositions)));
        fieldType.setOmitNorms(getBool(options.get(omitNorms)));
        String idxOptions = options.get(indexOptions);
        if (StringUtils.isNotBlank(idxOptions)) {
            fieldType.setIndexOptions(FieldInfo.IndexOptions.valueOf(idxOptions));
        }
        setNumericType(validator, fieldType);
        if (fieldType.numericType() != null) {
            String numPrecision = options.get(numericPrecisionStep);
            if (StringUtils.isNotEmpty(numPrecision) && StringUtils.isNumeric(numPrecision)) {
                fieldType.setNumericPrecisionStep(Integer.parseInt(numPrecision));
            }
        }
        return fieldType;
    }

    public static boolean getBool(String value) {
        return Boolean.parseBoolean(value);
    }

    public static BinaryDocValues getPKDocValues(IndexSearcher searcher) throws IOException {
        AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(searcher.getIndexReader());
        return wrapper.getBinaryDocValues(PK_NAME_DOC_VAL);
    }

    public static NumericDocValues getTSDocValues(IndexSearcher searcher) throws IOException {
        AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(searcher.getIndexReader());
        return wrapper.getNumericDocValues(CF_TS_DOC_VAL);
    }

    public static ByteBuffer primaryKey(BinaryDocValues rowKeyValues, int docId) throws IOException {
        BytesRef ref = new BytesRef();
        rowKeyValues.get(docId, ref);
        return ByteBuffer.wrap(ref.bytes, ref.offset, ref.length);
    }

    public static long timestamp(NumericDocValues tsValues, int docId) throws IOException {
        return tsValues.get(docId);
    }

    public static Field idField(final AbstractType abstractType, final ByteBuffer byteBufferValue, FieldType fieldType) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        BinaryTokenStream tokenStream = new BinaryTokenStream(bytesRef);
        return new Field(PK_NAME_INDEXED, tokenStream, fieldType) {
            @Override
            public String toString() {
                return String.format("PK Indexed Field<%s>", abstractType.getString(byteBufferValue));
            }

        };
    }

    public static Field idDocValues(final AbstractType abstractType, final ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        return new BinaryDocValuesField(PK_NAME_DOC_VAL, bytesRef) {
            @Override
            public String toString() {
                return String.format("PK BinaryDocValuesField<%s>", abstractType.getString(byteBufferValue));
            }
        };
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

    public static Field tsDocValues(final String colName, final long timestamp) {
        return new NumericDocValuesField(colName + TS_DOC_VAL, timestamp) {
            @Override
            public String toString() {
                return String.format("Timestamp NumericDocValuesField<%s> for column[<%s>]", timestamp, colName);
            }
        };
    }

    public static Term idTerm(ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        return new Term(PK_NAME_INDEXED, bytesRef);
    }

    public static Term tsTerm(long ts) {
        BytesRef tsBytes = new BytesRef();
        NumericUtils.longToPrefixCodedBytes(ts, NumericUtils.PRECISION_STEP_DEFAULT, tsBytes);
        return new Term(CF_TS_INDEXED, tsBytes);
    }

    public static boolean isNumeric(AbstractType type) {
        CQL3Type cqlType = type.asCQL3Type();
        if ((cqlType == CQL3Type.Native.INT) || (cqlType == CQL3Type.Native.VARINT) || (cqlType == CQL3Type.Native.BIGINT)
                || (cqlType == CQL3Type.Native.DECIMAL) || (cqlType == CQL3Type.Native.DOUBLE) || (cqlType == CQL3Type.Native.FLOAT)) {
            return true;
        }
        return false;
    }

    public static Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
        if (fieldType.docValueType() != null) {
            return docValueField(name, type, byteBufferValue);
        }
        CQL3Type cqlType = type.asCQL3Type();
        if (cqlType == CQL3Type.Native.INT) {
            return new IntField(name, (Integer) type.compose(byteBufferValue), fieldType);
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            return new LongField(name, ((Number) type.compose(byteBufferValue)).longValue(), fieldType);
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            return new DoubleField(name, ((Number) type.compose(byteBufferValue)).doubleValue(), fieldType);
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            return new FloatField(name, ((Number) type.compose(byteBufferValue)).floatValue(), fieldType);
        } else if (cqlType == CQL3Type.Native.TEXT || cqlType == CQL3Type.Native.VARCHAR) {
            return new Field(name, type.compose(byteBufferValue).toString(), fieldType);
        } else if (cqlType == CQL3Type.Native.UUID) {
            return new Field(name, type.compose(byteBufferValue).toString(), fieldType);
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            return new LongField(name, ((Date) type.compose(byteBufferValue)).getTime(), fieldType);
        } else if (cqlType == CQL3Type.Native.BOOLEAN) {
            Boolean val = ((Boolean) type.compose(byteBufferValue));
            return new Field(name, val.toString(), fieldType);
        } else {
            return new Field(name, ByteBufferUtil.getArray(byteBufferValue), fieldType);
        }
    }

    public static void setNumericType(AbstractType type, FieldType fieldType) {
        CQL3Type cqlType = type.asCQL3Type();
        if (cqlType == CQL3Type.Native.INT) {
            fieldType.setNumericType(FieldType.NumericType.INT);
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            fieldType.setNumericType(FieldType.NumericType.LONG);
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            fieldType.setNumericType(FieldType.NumericType.DOUBLE);
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            fieldType.setNumericType(FieldType.NumericType.FLOAT);
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            fieldType.setNumericType(FieldType.NumericType.LONG);
        }
    }


    private static Field docValueField(String name, final AbstractType abstractType, final ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        final String stripedName = striped + name;
        return new BinaryDocValuesField(stripedName, bytesRef) {
            @Override
            public String toString() {
                return String.format("BinaryDocValuesField <%s> <%s>", stripedName, abstractType.getString(byteBufferValue));
            }
        };
    }


}
