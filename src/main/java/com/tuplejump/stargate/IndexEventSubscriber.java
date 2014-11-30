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

import net.openhft.chronicle.ExcerptTailer;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnSerializer;
import org.apache.cassandra.db.TreeMapBackedSortedColumns;
import org.apache.cassandra.net.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: satya
 */
class IndexEventSubscriber extends Thread {
    protected static final Logger logger = LoggerFactory.getLogger(IndexEventBus.class);
    ExcerptTailer tailer;
    AtomicBoolean stopped;
    AtomicBoolean started;
    IndexingService indexingService;


    public IndexEventSubscriber(IndexingService indexingService, ExcerptTailer tailer) throws IOException {
        this.indexingService = indexingService;
        this.tailer = tailer;
        stopped = new AtomicBoolean(false);
        started = new AtomicBoolean(false);
        setName("SGIndex Entry Subscriber");
    }


    @Override
    public synchronized void start() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopped.set(true);
            }
        });
        super.start();
        started.set(true);
        IndexEventBus.logger.warn("********* Indexer Thread Started ************");
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (stopped.get()) break;
                while (tailer.nextIndex()) {
                    final ByteBuffer rowkeyBuffer = readRowKey(tailer);
                    final ColumnFamily columnFamily = ColumnFamily.serializer.deserialize(tailer, TreeMapBackedSortedColumns.factory, ColumnSerializer.Flag.LOCAL, MessagingService.current_version);
                    indexingService.index(rowkeyBuffer, columnFamily);
                }
                Thread.yield();
            } catch (Exception e) {
                logger.error("Error occurred while indexing row", e);
            }
        }

    }

    private ByteBuffer readRowKey(ExcerptTailer tailer) {
        int rowKeyLength = tailer.readInt();
        byte[] bytes = new byte[rowKeyLength];
        tailer.read(bytes);
        return ByteBuffer.wrap(bytes);
    }

}
