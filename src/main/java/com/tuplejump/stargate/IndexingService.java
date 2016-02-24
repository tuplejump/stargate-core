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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.tuplejump.stargate.cassandra.RowIndexSupport;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: satya
 */
public class IndexingService {
    protected static final Logger logger = LoggerFactory.getLogger(Stargate.class);
    ExecutorService executorService;
    Map<String, RowIndexSupport> support;

    IndexEntryEvent.Factory eventFactory = new IndexEntryEvent.Factory();
    int numWorkers = Math.max(4, Runtime.getRuntime().availableProcessors());
    // Specify the size of the ring buffer, must be power of 2.
    int bufferSize = 128 * numWorkers;
    Disruptor<IndexEntryEvent> disruptor;
    RingBuffer<IndexEntryEvent> ringBuffer;
    AtomicLong reads;
    AtomicLong writes;

    public IndexingService(AtomicLong reads, AtomicLong writes) {
        support = new HashMap<>();
        this.reads = reads;
        this.writes = writes;
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("SGRingBuffer-Thread-%d").build();
        executorService = Executors.newFixedThreadPool(numWorkers, namedThreadFactory);
        disruptor = new Disruptor<>(eventFactory, bufferSize, executorService);
        ringBuffer = disruptor.getRingBuffer();

        ExceptionHandler exceptionHandler = new FatalExceptionHandler();
        disruptor.handleExceptionsWith(exceptionHandler);

        EventHandler<IndexEntryEvent>[] eventHandlers = new EventHandler[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            eventHandlers[i] = new IndexEventHandler(this, i, numWorkers);
        }
        disruptor.handleEventsWith(eventHandlers);

        disruptor.start();
    }

    public void register(RowIndexSupport rowIndexSupport) {
        this.support.put(rowIndexSupport.tableMapper.cfMetaData.cfName, rowIndexSupport);
    }


    public void index(ByteBuffer rowkeyBuffer, ColumnFamily columnFamily) {
        long sequence = ringBuffer.next();
        try {
            // Get the entry in the Disruptor
            // for the sequence
            IndexEntryEvent event = ringBuffer.get(sequence);
            event.setData(rowkeyBuffer, columnFamily);
        } finally {
            ringBuffer.publish(sequence);
        }

    }


    public void updateAllIndexers() {
        for (RowIndexSupport rowIndexSupport : support.values()) {
            updateIndexers(rowIndexSupport);
        }
    }

    public void updateIndexers(RowIndexSupport rowIndexSupport) {
        if (rowIndexSupport.indexContainer instanceof MonolithIndexContainer) {
            rowIndexSupport.indexContainer.updateIndexers(null);
        } else if (StorageService.instance.isInitialized()) {
            Collection<Range<Token>> ranges = StorageService.instance.getLocalRanges(rowIndexSupport.keyspace);
            rowIndexSupport.indexContainer.updateIndexers(ranges);
        } else {
            AttributeChangeNotificationFilter attributeChangeNotificationFilter = new AttributeChangeNotificationFilter();
            attributeChangeNotificationFilter.enableAttribute("initialized");
            StorageService.instance.addNotificationListener(new NotificationListener() {
                @Override
                public void handleNotification(Notification notification, Object rowIdxSupport) {
                    AttributeChangeNotification attributeChangeNotification = (AttributeChangeNotification) notification;
                    if (attributeChangeNotification.getNewValue() == true) {
                        RowIndexSupport rowIndexSupport = (RowIndexSupport) rowIdxSupport;
                        Collection<Range<Token>> ranges = StorageService.instance.getLocalRanges(rowIndexSupport.keyspace);
                        rowIndexSupport.indexContainer.updateIndexers(ranges);
                    }
                }
            }, attributeChangeNotificationFilter, rowIndexSupport);
        }
    }

    private class FatalExceptionHandler implements ExceptionHandler {
        @Override
        public void handleEventException(final Throwable ex, final long sequence, final Object event) {
            logger.error("Exception processing: " + sequence + " " + event, ex);

            throw new RuntimeException(ex);
        }

        @Override
        public void handleOnStartException(final Throwable ex) {
            logger.error("Exception during onStart()", ex);
        }

        @Override
        public void handleOnShutdownException(final Throwable ex) {
            logger.error("Exception during onShutdown()", ex);
        }
    }

}
