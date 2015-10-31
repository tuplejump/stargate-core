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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import com.tuplejump.stargate.cassandra.RowIndexSupport;
import org.apache.cassandra.db.ColumnFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * User: satya
 */
class IndexEventSubscriber implements WorkHandler<IndexEntryEvent> {
    protected static final Logger logger = LoggerFactory.getLogger(Stargate.class);
    IndexingService indexingService;


    public IndexEventSubscriber(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public void onEvent(IndexEntryEvent event) throws Exception {
        ByteBuffer rowkeyBuffer = event.getRowKey();
        ColumnFamily columnFamily = event.getColumnFamily();

        final RowIndexSupport rowIndexSupport = indexingService.support.get(columnFamily.metadata().cfName);
        try {
            rowIndexSupport.indexRow(rowkeyBuffer, columnFamily);
        } catch (Exception e) {
            logger.error("Error occurred while indexing row of [" + columnFamily.metadata().cfName + "]", e);
        } finally {
            event.setData(null, null);
        }

        long readGen = indexingService.reads.incrementAndGet();
        if (logger.isDebugEnabled())
            logger.debug("Read gen:" + readGen);
    }
}
