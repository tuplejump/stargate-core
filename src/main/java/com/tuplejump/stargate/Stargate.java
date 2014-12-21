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
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.*;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * User: satya
 */
public class Stargate implements IEndpointStateChangeSubscriber, StargateMBean {
    protected static final Logger logger = LoggerFactory.getLogger(Stargate.class);
    private final static Stargate INSTANCE = new Stargate();

    static {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(INSTANCE, new ObjectName(MBEAN_NAME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final String COMMIT_LOGS = "commit-logs";
    public static Exception constructionException;
    IndexEventSubscriber indexEventSubscriber;
    Atomic.Long writes;
    IndexingService indexingService;
    DB db;
    BlockingQueue<IndexEntryEvent> queue;


    private Stargate() {
        try {
            queue = getQueue();
            writes = getAtomicLong(COMMIT_LOGS + "writes");
            indexingService = new IndexingService(getAtomicLong(COMMIT_LOGS + "reads"));
            indexEventSubscriber = new IndexEventSubscriber(indexingService, queue);
            Gossiper.instance.register(this);
        } catch (IOException e) {
            constructionException = e;
        }
    }

    public Atomic.Long getAtomicLong(String name) {
        if (db.exists(name)) return db.getAtomicLong(name);
        return db.createAtomicLong(name, 0l);
    }

    public static Stargate getInstance() {
        if (constructionException != null) throw new RuntimeException(constructionException);
        return INSTANCE;
    }

    public void register(RowIndexSupport rowIndexSupport) {
        this.indexingService.register(rowIndexSupport);
        if (StorageService.instance.isInitialized()) {
            indexingService.updateIndexers(rowIndexSupport);
            if (!indexEventSubscriber.started.get()) indexEventSubscriber.start();
        }
    }

    public long publish(ByteBuffer rowKey, ColumnFamily columnFamily) {
        queue.offer(new IndexEntryEvent(rowKey, columnFamily));
        long writeGen = writes.incrementAndGet();
        if (logger.isDebugEnabled())
            logger.debug("Write gen:" + writeGen);
        return writeGen;
    }


    public void catchUp(long latest) {
        while (true) {
            if (indexingService.reads.get() >= latest) break;
        }
    }

    private BlockingQueue<IndexEntryEvent> getQueue() throws IOException {
        db = getDB();
        BlockingQueue<IndexEntryEvent> queue;
        if (db.exists(COMMIT_LOGS)) {
            queue = db.getQueue(COMMIT_LOGS);
        } else {
            queue = db.createQueue(COMMIT_LOGS, new IndexEntryEvent.IndexEntryEventSerializer(), false);
        }
        return queue;

    }

    private DB getDB() throws IOException {
        String dirName = Options.defaultIndexesDir;
        dirName = dirName + File.separator + COMMIT_LOGS;
        File dir = new File(dirName);
        boolean createNew = false;
        if (!dir.exists()) {
            createNew = dir.mkdirs();
        }
        logger.warn("##### Index commit log directory path #####[" + dir.getPath() + "]. Created new [" + createNew + "]");
        File file = new File(dir, "log");
        if (!file.exists()) {
            createNew = file.createNewFile();
        }
        logger.warn("##### Index commit log file path #####[" + file.getPath() + "]. Created new [" + createNew + "]");
        return DBMaker.
                newFileDB(file)
                .mmapFileEnableIfSupported()
                .closeOnJvmShutdown()
                .make();
    }


    @Override
    public void onJoin(InetAddress endpoint, EndpointState epState) {
    }

    @Override
    public void beforeChange(InetAddress endpoint, EndpointState currentState, ApplicationState newStateKey, VersionedValue newValue) {
    }

    @Override
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value) {
        if (state == ApplicationState.TOKENS && FBUtilities.getBroadcastAddress().equals(endpoint)) {
            indexingService.updateAllIndexers();
            if (!indexEventSubscriber.started.get()) indexEventSubscriber.start();
        }
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


    @Override
    public String[] allIndexes() {
        String[] allIndexes = new String[indexingService.support.size()];
        int i = 0;
        for (Map.Entry<String, RowIndexSupport> entry : indexingService.support.entrySet()) {
            RowIndexSupport rowIndexSupport = entry.getValue();
            allIndexes[i++] = rowIndexSupport.indexContainer.indexName;
        }
        return allIndexes;
    }

    @Override
    public String[] indexShards(String indexName) {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(indexName);
        if (indexSupport != null) {
            Set<Range<Token>> indexShards = indexSupport.indexContainer.indexers.keySet();
            String[] indexRanges = new String[indexShards.size()];
            int i = 0;
            for (Range<Token> indexRange : indexShards) {
                indexRanges[i++] = indexRange.toString();
            }
            return indexRanges;
        }
        return null;
    }

    @Override
    public String describeIndex(String indexName) throws IOException {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(indexName);
        if (indexSupport != null) {
            return indexSupport.getOptions().describeAsJson();
        }
        return null;
    }

    private RowIndexSupport getRowIndexSupportByIndexName(String indexName) {
        for (Map.Entry<String, RowIndexSupport> entry : indexingService.support.entrySet()) {
            RowIndexSupport rowIndexSupport = entry.getValue();
            if (rowIndexSupport.indexContainer.indexName.equalsIgnoreCase(indexName)) return rowIndexSupport;
        }
        return null;
    }

    @Override
    public long indexLiveSize(String indexName) {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(indexName);
        if (indexSupport != null) {
            return indexSupport.indexContainer.liveSize();
        }
        return 0;
    }

    @Override
    public long indexSize(String indexName) {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(indexName);
        if (indexSupport != null) {
            return indexSupport.indexContainer.size();
        }
        return 0;
    }

    @Override
    public long writeGeneration() {
        return writes.get();
    }

    @Override
    public long readGeneration() {
        return indexingService.reads.get();
    }


}
