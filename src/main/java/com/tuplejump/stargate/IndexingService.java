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
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.StorageService;
import org.mapdb.Atomic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: satya
 */
public class IndexingService {
    protected static final Logger logger = LoggerFactory.getLogger(Stargate.class);
    ExecutorService executorService;
    Map<String, RowIndexSupport> support;
    Atomic.Long reads;


    public IndexingService(Atomic.Long reads) {
        support = new HashMap<>();
        this.reads = reads;
        executorService = Executors.newFixedThreadPool(4);
    }

    public void register(RowIndexSupport rowIndexSupport) {
        this.support.put(rowIndexSupport.getCFMetaData().cfName, rowIndexSupport);
    }

    public void index(IndexEntryEvent entryEvent) {
        final ByteBuffer rowkeyBuffer = entryEvent.rowKey;
        final ColumnFamily columnFamily = entryEvent.columnFamily;
        final IndexEntryEvent.Type type = entryEvent.type;
        final RowIndexSupport rowIndexSupport = support.get(columnFamily.metadata().cfName);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    rowIndexSupport.indexRow(rowkeyBuffer, columnFamily);
                } catch (Exception e) {
                    logger.error("Error occurred while indexing row of [" + columnFamily.metadata().cfName + "]", e);
                }

                long readGen = reads.incrementAndGet();
                if (logger.isDebugEnabled())
                    logger.debug("Read gen:" + readGen);
            }
        });

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
