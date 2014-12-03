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

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnSerializer;
import org.apache.cassandra.db.TreeMapBackedSortedColumns;
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
    ByteBuffer rowKey;
    ColumnFamily columnFamily;

    public IndexEntryEvent(ByteBuffer rowKey, ColumnFamily columnFamily) {
        this.rowKey = rowKey;
        this.columnFamily = columnFamily;
    }

    public ByteBuffer getRowKey() {
        return rowKey;
    }

    public ColumnFamily getColumnFamily() {
        return columnFamily;
    }

    public static class IndexEntryEventSerializer implements Serializer<IndexEntryEvent>, Serializable {

        @Override
        public void serialize(DataOutput appender, IndexEntryEvent value) throws IOException {
            appender.writeInt(value.rowKey.remaining());
            ByteBufferUtil.write(value.rowKey, appender);
            ColumnFamily.serializer.serialize(value.columnFamily, appender, MessagingService.current_version);
        }

        @Override
        public IndexEntryEvent deserialize(DataInput in, int available) throws IOException {
            int rowKeyLength = in.readInt();
            ByteBuffer rowKeyBuffer = ByteBufferUtil.read(in, rowKeyLength);
            ColumnFamily columnFamily = ColumnFamily.serializer.deserialize(in, TreeMapBackedSortedColumns.factory, ColumnSerializer.Flag.LOCAL, MessagingService.current_version);
            return new IndexEntryEvent(rowKeyBuffer, columnFamily);
        }

        @Override
        public int fixedSize() {
            //variable sized record
            return -1;
        }
    }
}
