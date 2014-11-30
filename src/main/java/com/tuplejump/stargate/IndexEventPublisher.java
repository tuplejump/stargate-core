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
import com.tuplejump.stargate.lucene.Options;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnSerializer;
import org.apache.cassandra.db.TreeMapBackedSortedColumns;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.*;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: satya
 */
public class IndexEventPublisher {
    protected static final Logger logger = LoggerFactory.getLogger(RowIndex.class);
    VanillaChronicle chronicle;
    IndexEventSubscriber indexEventSubscriber;
    Map<CFMetaData, RowIndexSupport> support = new HashMap<>();

    private final static IndexEventPublisher INSTANCE = new IndexEventPublisher();
    public static RuntimeException constructionException;

    private AtomicLong writes;
    private AtomicLong reads;

    public static IndexEventPublisher getInstance() {
        if (constructionException != null) throw constructionException;
        else return INSTANCE;
    }


    public long writeEvent(ByteBuffer rowKey, ColumnFamily columnFamily) {
        try {
            ExcerptAppender appender = chronicle.createAppender();
            int rowKeyLength = rowKey.remaining();
            appender.startExcerpt();
            appender.writeInt(rowKeyLength);
            appender.write(rowKey);
            ColumnFamily.serializer.serialize(columnFamily, appender, MessagingService.current_version);
            appender.finish();
            long writeGen = writes.incrementAndGet();
            if (logger.isDebugEnabled())
                logger.debug("Write gen:" + writeGen);
            return writeGen;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void catchUp(long latest) {
        while (true) {
            if (reads.get() >= latest) break;
        }
    }

    public void catchUp() {
        while (true) {
            if (reads.get() == writes.get()) break;
        }
    }

    public void register(RowIndexSupport rowIndexSupport) {
        this.support.put(rowIndexSupport.getCFMetaData(), rowIndexSupport);
        if (StorageService.instance.isInitialized()) {
            Collection<Range<Token>> ranges = StorageService.instance.getLocalRanges(rowIndexSupport.keyspace);
            rowIndexSupport.indexContainer.updateIndexers(ranges);
            if (!indexEventSubscriber.started.get()) indexEventSubscriber.start();
        }

    }

    private IndexEventPublisher() {
        try {
            chronicle = createChronicle();
            indexEventSubscriber = new IndexEventSubscriber(chronicle);
            support = new HashMap<>();
            writes = new AtomicLong(0);
            reads = new AtomicLong(0);
            RingChangeListener changeListener = new RingChangeListener();
            Gossiper.instance.register(changeListener);
        } catch (Exception e) {
            constructionException = new RuntimeException(e);
        }
    }


    private VanillaChronicle createChronicle() {
        String dirName = Options.defaultIndexesDir;
        dirName = dirName + File.separator + "commit-logs";
        File file = new File(dirName);
        if (!file.exists()) file.mkdirs();
        logger.warn("##### Chronicle file path #####[" + file.getPath() + "]");
        return new VanillaChronicle(file.getPath());
    }

    private class IndexEventSubscriber extends Thread {
        ExcerptTailer tailer;
        AtomicBoolean stopped;
        AtomicBoolean started;

        public IndexEventSubscriber(VanillaChronicle chronicle) throws IOException {
            tailer = chronicle.createTailer();
            stopped = new AtomicBoolean(false);
            started = new AtomicBoolean(false);
            setName("SGIndex Entry Subscriber");
        }


        @Override
        public synchronized void start() {
            super.start();
            started.set(true);
            logger.warn("********* Indexer Thread Started ************");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (stopped.get()) break;
                    while (tailer.nextIndex()) {
                        indexRow();
                        long readGen = reads.incrementAndGet();
                        if (logger.isDebugEnabled())
                            logger.debug("Read gen:" + readGen);
                    }
                    Thread.yield();
                } catch (Exception e) {
                    logger.error("Error occurred while indexing row", e);
                }
            }

        }

        //since this is a single thread, we can recycle one BB
        final ByteBuffer rowkeyBuffer = ByteBuffer.allocateDirect(5 * 1024).order(ByteOrder.nativeOrder());

        private void indexRow() throws IOException {
            readRowKey();
            ColumnFamily columnFamily = ColumnFamily.serializer.deserialize(tailer, TreeMapBackedSortedColumns.factory, ColumnSerializer.Flag.LOCAL, MessagingService.current_version);
            RowIndexSupport rowIndexSupport = support.get(columnFamily.metadata());
            rowIndexSupport.indexRow(rowkeyBuffer, columnFamily);
        }

        private void readRowKey() {
            int rowKeyLength = tailer.readInt();
            rowkeyBuffer.clear();
            rowkeyBuffer.limit(rowKeyLength);
            tailer.read(rowkeyBuffer);
            rowkeyBuffer.flip();
        }
    }


    protected class RingChangeListener implements IEndpointStateChangeSubscriber {

        private void doOnChange() {
            for (RowIndexSupport rowIndexSupport : support.values()) {
                Collection<Range<Token>> ranges = StorageService.instance.getLocalRanges(rowIndexSupport.keyspace);
                rowIndexSupport.indexContainer.updateIndexers(ranges);
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    indexEventSubscriber.stopped.set(true);
                }
            });
            if (!indexEventSubscriber.started.get()) indexEventSubscriber.start();
        }


        @Override
        public void onJoin(InetAddress endpoint, EndpointState epState) {

        }

        @Override
        public void beforeChange(InetAddress endpoint, EndpointState currentState, ApplicationState newStateKey, VersionedValue newValue) {

        }

        @Override
        public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value) {
            if (state == ApplicationState.TOKENS && FBUtilities.getBroadcastAddress().equals(endpoint))
                doOnChange();
        }

        @Override
        public void onAlive(InetAddress endpoint, EndpointState state) {

        }

        @Override
        public void onDead(InetAddress endpoint, EndpointState state) {
            if (FBUtilities.getBroadcastAddress().equals(endpoint))
                indexEventSubscriber.stopped.set(true);

        }

        @Override
        public void onRemove(InetAddress endpoint) {

        }

        @Override
        public void onRestart(InetAddress endpoint, EndpointState state) {

        }
    }


}
