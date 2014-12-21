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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: satya
 */
class IndexEventSubscriber extends Thread {
    protected static final Logger logger = LoggerFactory.getLogger(Stargate.class);
    BlockingQueue<IndexEntryEvent> queue;
    AtomicBoolean stopped;
    AtomicBoolean started;
    IndexingService indexingService;


    public IndexEventSubscriber(IndexingService indexingService, BlockingQueue<IndexEntryEvent> queue) {
        this.indexingService = indexingService;
        this.queue = queue;
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
        Stargate.logger.warn("********* Indexer Thread Started ************");
    }

    @Override
    public void run() {
        while (!stopped.get()) {
            try {
                if (queue.peek() != null) {
                    IndexEntryEvent indexEntryEvent = queue.remove();
                    indexingService.index(indexEntryEvent.getRowKey(), indexEntryEvent.getColumnFamily());
                }
                Thread.yield();
            } catch (Exception e) {
                logger.error("Error occurred while indexing row", e);
            }
        }

    }

}
