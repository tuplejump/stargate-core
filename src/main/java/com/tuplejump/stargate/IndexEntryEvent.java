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

import com.lmax.disruptor.EventFactory;
import org.apache.cassandra.db.ColumnFamily;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * User: satya
 */
public class IndexEntryEvent implements Serializable {
    private ByteBuffer rowKey;
    private ColumnFamily columnFamily;

    public IndexEntryEvent() {
    }

    public void setData(ByteBuffer rowKey, ColumnFamily columnFamily) {
        this.rowKey = rowKey;
        this.columnFamily = columnFamily;
    }

    public ByteBuffer getRowKey() {
        return rowKey;
    }

    public ColumnFamily getColumnFamily() {
        return columnFamily;
    }

    public static class Factory implements EventFactory<IndexEntryEvent> {

        @Override
        public IndexEntryEvent newInstance() {
            return new IndexEntryEvent();
        }
    }


}
