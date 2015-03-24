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

package com.tuplejump.stargate;

import org.apache.cassandra.db.AtomicBTreeColumns;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnSerializer;
import org.apache.cassandra.io.util.AbstractDataOutput;
import org.apache.cassandra.io.util.DataOutputPlus;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * User: satya
 */
public class IndexEntryEvent implements Serializable {
    public final ByteBuffer rowKey;
    public final ColumnFamily columnFamily;
    public final Type type;

    public IndexEntryEvent(Type type, ByteBuffer rowKey, ColumnFamily columnFamily) {
        this.type = type;
        this.rowKey = rowKey;
        this.columnFamily = columnFamily;
    }

    public static class IndexEntryEventSerializer implements Serializer<IndexEntryEvent>, Serializable {

        @Override
        public void serialize(final DataOutput appender, IndexEntryEvent value) throws IOException {
            appender.writeByte(value.type.getByte());
            appender.writeInt(value.rowKey.remaining());
            Utils.write(value.rowKey, appender);
            DataOutputPlus dataOutputPlus = new AbstractDataOutput() {
                @Override
                public void write(byte[] buffer, int offset, int count) throws IOException {
                    appender.write(buffer, offset, count);
                }

                @Override
                public void write(int oneByte) throws IOException {
                    appender.write(oneByte);
                }
            };
            ColumnFamily.serializer.serialize(value.columnFamily, dataOutputPlus, MessagingService.current_version);
        }

        @Override
        public IndexEntryEvent deserialize(DataInput in, int available) throws IOException {
            Type type = Type.fromByte(in.readByte());
            int rowKeyLength = in.readInt();
            ByteBuffer rowKeyBuffer = ByteBufferUtil.read(in, rowKeyLength);
            ColumnFamily columnFamily = ColumnFamily.serializer.deserialize(in, AtomicBTreeColumns.factory, ColumnSerializer.Flag.LOCAL, MessagingService.current_version);
            return new IndexEntryEvent(type, rowKeyBuffer, columnFamily);
        }

        @Override
        public int fixedSize() {
            //variable sized record
            return -1;
        }
    }

    /**
     * User: satya
     */
    public static enum Type {
        UPSERT((byte) 0x01);

        private final byte b;

        Type(byte b) {
            this.b = b;
        }

        // useful to write to a ChannelBuffer, when encoding
        public byte getByte() {
            return this.b;
        }

        // useful to determine which enum value matches byte, when decoding
        public static Type fromByte(byte b) {
            for (Type type : Type.values()) {
                if (type.b == b) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No Type for byte: " + b);
        }

    }

}
