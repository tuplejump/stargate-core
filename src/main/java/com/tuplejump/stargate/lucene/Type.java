package com.tuplejump.stargate.lucene;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

import java.nio.ByteBuffer;

/**
 * Created by satya on 23/02/16.
 */
public enum Type {
    object(FieldCreator.COMPOSITE),
    map(FieldCreator.COMPOSITE),
    text(FieldCreator.STRING),
    string(FieldCreator.STRING),
    integer(FieldCreator.INT),
    bigint(FieldCreator.LONG),
    decimal(FieldCreator.FLOAT),
    bigdecimal(FieldCreator.DOUBLE),
    bool(FieldCreator.STRING),
    timeuuid(FieldCreator.TIME_UUID),
    uuid(FieldCreator.UUID),
    date(FieldCreator.TIMESTAMP);

    public final FieldCreator fieldCreator;

    Type(FieldCreator fieldCreator) {
        this.fieldCreator = fieldCreator;
    }

    public boolean isNumeric() {
        return this == bigint || this == bigdecimal || this == integer || this == decimal;
    }

    public boolean isCharSeq() {
        return this == string || this == text;
    }

    public boolean canTokenize() {
        Type type = this;
        return !(type.isNumeric() || type == Type.string || type == Type.date || type == Type.bool);
    }


}

