package com.tuplejump.stargate;

import com.tuplejump.stargate.lucene.Properties;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import static com.tuplejump.stargate.Constants.*;

/**
 * User: satya
 * <p/>
 * Utility methods to deal in fields.
 */
public class Fields {
    public static SortedDocValues getPKDocValues(IndexSearcher searcher) throws IOException {
        AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(searcher.getIndexReader());
        return wrapper.getSortedDocValues(PK_NAME_DOC_VAL);
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

    public static Field idDocValues(final AbstractType abstractType, final ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        return new SortedDocValuesField(PK_NAME_DOC_VAL, bytesRef) {
            @Override
            public String toString() {
                return String.format("PK BinaryDocValuesField<%s>", abstractType.getString(byteBufferValue));
            }
        };
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

    public static Term idTerm(ByteBuffer byteBufferValue) {
        BytesRef bytesRef = new BytesRef(byteBufferValue.array(), byteBufferValue.arrayOffset(), byteBufferValue.limit());
        return new Term(PK_NAME_INDEXED, bytesRef);
    }

    public static Term tsTerm(long ts) {
        BytesRef tsBytes = new BytesRef();
        NumericUtils.longToPrefixCodedBytes(ts, NumericUtils.PRECISION_STEP_DEFAULT, tsBytes);
        return new Term(CF_TS_INDEXED, tsBytes);
    }

    public static Field field(String name, AbstractType type, Object composedValue, FieldType fieldType) {
        CQL3Type cqlType = type.asCQL3Type();
        if (cqlType == CQL3Type.Native.INT) {
            return new IntField(name, (Integer) composedValue, fieldType);
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            return new LongField(name, ((Number) composedValue).longValue(), fieldType);
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            return new DoubleField(name, ((Number) composedValue).doubleValue(), fieldType);
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            return new FloatField(name, ((Number) composedValue).floatValue(), fieldType);
        } else if (cqlType == CQL3Type.Native.TEXT || cqlType == CQL3Type.Native.VARCHAR) {
            return new Field(name, composedValue.toString(), fieldType);
        } else if (cqlType == CQL3Type.Native.UUID) {
            return new Field(name, composedValue.toString(), fieldType);
        } else if (cqlType == CQL3Type.Native.TIMEUUID) {
            //TODO TimeUUID toString is not comparable.
            return new Field(name, composedValue.toString(), fieldType);
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            return new LongField(name, ((Date) composedValue).getTime(), fieldType);
        } else if (cqlType == CQL3Type.Native.BOOLEAN) {
            Boolean val = ((Boolean) composedValue);
            return new Field(name, val.toString(), fieldType);
        } else {
            return new Field(name, ByteBufferUtil.getArray((ByteBuffer) composedValue), fieldType);
        }
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
        } else if (cqlType == CQL3Type.Native.TIMEUUID) {
            //TODO TimeUUID toString is not comparable.
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
            //TODO
            return new LongField(name, Date.parse(value), fieldType);
        } else if (type == Properties.Type.bool) {
            Boolean val = Boolean.parseBoolean(value);
            return new Field(name, val.toString(), fieldType);
        } else {
            return new Field(name, value, fieldType);
        }
    }

    public static void setNumericType(AbstractType type, FieldType luceneFieldType) {
        CQL3Type cqlType = type.asCQL3Type();
        if (cqlType == CQL3Type.Native.INT) {
            luceneFieldType.setNumericType(FieldType.NumericType.INT);
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            luceneFieldType.setNumericType(FieldType.NumericType.LONG);
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            luceneFieldType.setNumericType(FieldType.NumericType.DOUBLE);
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            luceneFieldType.setNumericType(FieldType.NumericType.FLOAT);
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            luceneFieldType.setNumericType(FieldType.NumericType.LONG);
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
