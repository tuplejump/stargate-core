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
import org.apache.cassandra.gms.*;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
    IndexingService indexingService;

    private Stargate() {
        try {
            indexingService = new IndexingService(getAtomicLong(COMMIT_LOGS + "reads"), getAtomicLong(COMMIT_LOGS + "writes"));
            Gossiper.instance.register(this);
        } catch (Exception e) {
            constructionException = e;
        }
    }

    public AtomicLong getAtomicLong(String name) {
        return new AtomicLong();
    }

    public static Stargate getInstance() {
        if (constructionException != null) throw new RuntimeException(constructionException);
        return INSTANCE;
    }

    public void register(RowIndexSupport rowIndexSupport) {
        this.indexingService.register(rowIndexSupport);
        indexingService.updateIndexers(rowIndexSupport);
    }

    public long index(ByteBuffer rowKey, ColumnFamily columnFamily) {
        final RowIndexSupport rowIndexSupport = indexingService.support.get(columnFamily.metadata().ksAndCFName);
        try {
            rowIndexSupport.indexRow(rowKey, columnFamily);
        } catch (Exception e) {
            logger.error("Error occurred while indexing row of [" + columnFamily.metadata().cfName + "]", e);
        } finally {
            indexingService.reads.incrementAndGet();
        }
        long writeGen = indexingService.writes.incrementAndGet();
        if (logger.isDebugEnabled())
            logger.debug("Write gen:" + writeGen);
        return writeGen;
    }


    public long publish(ByteBuffer rowKey, ColumnFamily columnFamily) {
        indexingService.index(rowKey, columnFamily);
        long writeGen = indexingService.writes.incrementAndGet();
        if (logger.isDebugEnabled())
            logger.debug("Write gen:" + writeGen);
        return writeGen;
    }


    public void catchUp(long latest) {
        while (true) {
            if (indexingService.reads.get() >= latest) break;
        }
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
        }
    }

    @Override
    public void onAlive(InetAddress endpoint, EndpointState state) {
    }

    @Override
    public void onDead(InetAddress endpoint, EndpointState state) {
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
        for (Map.Entry<Pair<String, String>, RowIndexSupport> entry : indexingService.support.entrySet()) {
            RowIndexSupport rowIndexSupport = entry.getValue();
            allIndexes[i++] = entry.getKey().left + "." + rowIndexSupport.indexContainer.indexName();
        }
        return allIndexes;
    }

    @Override
    public String[] indexShards(String keyspaceName, String indexName) {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(keyspaceName, indexName);
        if (indexSupport != null && indexSupport.indexContainer instanceof PerVNodeIndexContainer) {
            PerVNodeIndexContainer indexContainer = (PerVNodeIndexContainer) indexSupport.indexContainer;
            Set<Range<Token>> indexShards = indexContainer.indexers.keySet();
            String[] indexRanges = new String[indexShards.size()];
            int i = 0;
            for (Range<Token> indexRange : indexShards) {
                indexRanges[i++] = indexRange.toString();
            }
            return indexRanges;
        }
        return new String[]{""};
    }

    @Override
    public String describeIndex(String keyspaceName, String indexName) throws IOException {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(keyspaceName, indexName);
        if (indexSupport != null) {
            return indexSupport.getOptions().describeAsJson();
        }
        return null;
    }

    private RowIndexSupport getRowIndexSupportByIndexName(String keyspaceName, String indexName) {
        for (Map.Entry<Pair<String, String>, RowIndexSupport> entry : indexingService.support.entrySet()) {
            RowIndexSupport rowIndexSupport = entry.getValue();
            if (entry.getKey().left.equalsIgnoreCase(keyspaceName) &&
                    rowIndexSupport.indexContainer.indexName().equalsIgnoreCase(indexName)) {
                return rowIndexSupport;
            }
        }
        return null;
    }

    @Override
    public long indexLiveSize(String keyspaceName, String indexName) {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(keyspaceName, indexName);
        if (indexSupport != null) {
            return indexSupport.indexContainer.liveSize();
        }
        return 0;
    }

    @Override
    public long indexSize(String keyspaceName, String indexName) {
        RowIndexSupport indexSupport = getRowIndexSupportByIndexName(keyspaceName, indexName);
        if (indexSupport != null) {
            return indexSupport.indexContainer.size();
        }
        return 0;
    }

    @Override
    public long writeGeneration() {
        return indexingService.writes.get();
    }

    @Override
    public long readGeneration() {
        return indexingService.reads.get();
    }


}
