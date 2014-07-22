package com.tuplejump.stargate;

import com.tuplejump.stargate.lucene.Properties;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BooleanType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.tuplejump.stargate.Constants.*;

/**
 * User: satya
 * <p/>
 * Utility methods to deal in fields.
 */
public class Fields {

    public static SortedDocValues getRKDocValues(AtomicReader atomicReader) throws IOException {
        return atomicReader.getSortedDocValues(PK_NAME_DOC_VAL);
    }

    public static SortedDocValues getPKDocValues(AtomicReader atomicReader) throws IOException {
        return atomicReader.getSortedDocValues(PK_NAME_STORED);
    }

    public static NumericDocValues getTSDocValues(AtomicReader atomicReader) throws IOException {
        return atomicReader.getNumericDocValues(CF_TS_DOC_VAL);
    }

    public static ByteBuffer rowKey(BinaryDocValues rowKeyValues, int docId) throws IOException {
        BytesRef ref = new BytesRef();
        rowKeyValues.get(docId, ref);
        return ByteBuffer.wrap(ref.bytes, ref.offset, ref.length);
    }

    public static String primaryKeyName(BinaryDocValues primaryKeyNames, int docId) throws IOException {
        BytesRef ref = new BytesRef();
        primaryKeyNames.get(docId, ref);
        return new String(ref.bytes, ref.offset, ref.length, StandardCharsets.UTF_8);
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

    public static Field pkNameDocValues(final String pkName) {
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
        } else if (cqlType == CQL3Type.Native.ASCII || cqlType == CQL3Type.Native.TEXT || cqlType == CQL3Type.Native.VARCHAR) {
            return new Field(name, type.getString(byteBufferValue), fieldType);
        } else if (cqlType == CQL3Type.Native.UUID) {
            return new Field(name, type.getString(byteBufferValue), fieldType);
        } else if (cqlType == CQL3Type.Native.TIMEUUID) {
            //TODO TimeUUID toString is not comparable.
            return new Field(name, type.getString(byteBufferValue), fieldType);
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            return new LongField(name, ((Date) type.compose(byteBufferValue)).getTime(), fieldType);
        } else if (cqlType == CQL3Type.Native.BOOLEAN) {
            Boolean val = ((Boolean) type.compose(byteBufferValue));
            return new Field(name, val.toString(), fieldType);
        } else {
            return new Field(name, toString(byteBufferValue, type), fieldType);
        }
    }

    public static ByteBuffer defaultValue(AbstractType type) {
        CQL3Type cqlType = type.asCQL3Type();
        if (cqlType == CQL3Type.Native.INT) {
            return ByteBufferUtil.bytes(0);
        } else if (cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT || cqlType == CQL3Type.Native.COUNTER) {
            return ByteBufferUtil.bytes(0L);
        } else if (cqlType == CQL3Type.Native.DECIMAL || cqlType == CQL3Type.Native.DOUBLE) {
            return ByteBufferUtil.bytes(0D);
        } else if (cqlType == CQL3Type.Native.FLOAT) {
            return ByteBufferUtil.bytes(0F);
        } else if (cqlType == CQL3Type.Native.TEXT || cqlType == CQL3Type.Native.VARCHAR) {
            return ByteBufferUtil.bytes("");
        } else if (cqlType == CQL3Type.Native.UUID) {
            return ByteBufferUtil.bytes(UUID.randomUUID());
        } else if (cqlType == CQL3Type.Native.TIMEUUID) {
            return ByteBufferUtil.bytes(UUID.randomUUID());
        } else if (cqlType == CQL3Type.Native.TIMESTAMP) {
            return ByteBufferUtil.bytes(0L);
        } else if (cqlType == CQL3Type.Native.BOOLEAN) {
            return BooleanType.instance.decompose(false);
        } else {
            return ByteBufferUtil.EMPTY_BYTE_BUFFER;
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

    public static String toString(ByteBuffer byteBuffer, AbstractType<?> type) {
        if (type instanceof CompositeType) {
            CompositeType composite = (CompositeType) type;
            List<AbstractType<?>> types = composite.types;
            ByteBuffer[] components = composite.split(byteBuffer);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < components.length; i++) {
                AbstractType<?> componentType = types.get(i);
                ByteBuffer component = components[i];
                sb.append(componentType.compose(component));
                if (i < types.size() - 1) {
                    sb.append(':');
                }
            }
            return sb.toString();
        } else {
            return type.compose(byteBuffer).toString();
        }
    }

}
