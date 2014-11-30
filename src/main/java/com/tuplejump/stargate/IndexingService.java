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

import com.tuplejump.stargate.cassandra.RowIndexSupport;
import net.openhft.chronicle.ExcerptTailer;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnSerializer;
import org.apache.cassandra.db.TreeMapBackedSortedColumns;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: satya
 */
public class IndexingService {
    protected static final Logger logger = LoggerFactory.getLogger(IndexEventBus.class);
    ExecutorService executorService;
    Map<String, RowIndexSupport> support;
    AtomicLong reads;


    public IndexingService() {
        support = new HashMap<>();
        reads = new AtomicLong(0);
        executorService = Executors.newFixedThreadPool(4);
    }

    public void register(RowIndexSupport rowIndexSupport) {
        this.support.put(rowIndexSupport.getCFMetaData().cfName, rowIndexSupport);
    }

    public void index(final ExcerptTailer tailer) {
        submitForIndex(tailer);
    }

    private void submitForIndex(final ExcerptTailer tailer) {
        try {
            final ByteBuffer rowkeyBuffer = readRowKey(tailer);
            final ColumnFamily columnFamily = ColumnFamily.serializer.deserialize(tailer, TreeMapBackedSortedColumns.factory, ColumnSerializer.Flag.LOCAL, MessagingService.current_version);
            final RowIndexSupport rowIndexSupport = support.get(columnFamily.metadata().cfName);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    rowIndexSupport.indexRow(rowkeyBuffer, columnFamily);
                    long readGen = reads.incrementAndGet();
                    if (logger.isDebugEnabled())
                        logger.debug("Read gen:" + readGen);
                }
            });

        } catch (IOException e) {
            logger.error("Exception occurred while de-serializing column family", e);
        }
    }


    private ByteBuffer readRowKey(ExcerptTailer tailer) {
        int rowKeyLength = tailer.readInt();
        byte[] bytes = new byte[rowKeyLength];
        tailer.read(bytes);
        return ByteBuffer.wrap(bytes);
    }

    public void updateAllIndexers() {
        for (RowIndexSupport rowIndexSupport : support.values()) {
            updateIndexers(rowIndexSupport);
        }
    }

    public void updateIndexers(RowIndexSupport rowIndexSupport) {
        Collection<Range<Token>> ranges = StorageService.instance.getLocalRanges(rowIndexSupport.keyspace);
        rowIndexSupport.indexContainer.updateIndexers(ranges);
    }

}
