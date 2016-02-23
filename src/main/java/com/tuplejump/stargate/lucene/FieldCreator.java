package com.tuplejump.stargate.lucene;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.TimeUUIDType;
import org.apache.cassandra.db.marshal.UUIDType;
import org.apache.lucene.document.*;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Created by satya on 23/02/16.
 */
public abstract class FieldCreator {

    public static FieldCreator STRING = new StringFieldCreator();
    public static FieldCreator INT = new IntFieldCreator();
    public static FieldCreator LONG = new LongFieldCreator();
    public static FieldCreator DOUBLE = new DoubleFieldCreator();
    public static FieldCreator FLOAT = new FloatFieldCreator();
    public static FieldCreator TIMESTAMP = new TimestampFieldCreator();
    public static FieldCreator UUID = new UUIDFieldCreator();
    public static FieldCreator TIME_UUID = new TimeUUIDFieldCreator();
    public static FieldCreator COMPOSITE = new CompositeFieldCreator();



    public static String reorderTimeUUId(String originalTimeUUID) {
        StringTokenizer tokens = new StringTokenizer(originalTimeUUID, "-");
        if (tokens.countTokens() == 5) {
            String time_low = tokens.nextToken();
            String time_mid = tokens.nextToken();
            String time_high_and_version = tokens.nextToken();
            String variant_and_sequence = tokens.nextToken();
            String node = tokens.nextToken();
            return time_high_and_version + '-' + time_mid + '-' + time_low + '-' + variant_and_sequence + '-' + node;
        }

        return originalTimeUUID;
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
    public abstract Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType);

    private static class StringFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new Field(name, value.toString(), fieldType);
        }

    }


    private static class IntFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new IntField(name, (Integer) value, fieldType);
        }
    }

    private static class LongFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new LongField(name, ((Number) value).longValue(), fieldType);
        }
    }

    private static class DoubleFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new DoubleField(name, ((Number) value).doubleValue(), fieldType);
        }
    }
    private static class FloatFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new FloatField(name, ((Number) value).floatValue(), fieldType);
        }
    }

    private static class TimestampFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new LongField(name, ((Date) value).getTime(), fieldType);
        }
    }
    private static class UUIDFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new Field(name, UUIDType.instance.getSerializer().toString((UUID) value), fieldType);
        }
    }

    private static class TimeUUIDFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new Field(name, reorderTimeUUId(TimeUUIDType.instance.getSerializer().toString((UUID) value)), fieldType);
        }
    }

    private static class CompositeFieldCreator extends FieldCreator{
        public Field field(String name, AbstractType type, ByteBuffer byteBufferValue, FieldType fieldType) {
            Object value = type.compose(byteBufferValue);
            return new Field(name, toString(byteBufferValue, type), fieldType);
        }
    }



}
