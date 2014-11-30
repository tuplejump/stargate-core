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
import net.openhft.chronicle.VanillaChronicle;
import org.apache.cassandra.db.ColumnFamily;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: satya
 */
public class IndexEventBus implements IEndpointStateChangeSubscriber {
    protected static final Logger logger = LoggerFactory.getLogger(IndexEventBus.class);
    private final static IndexEventBus INSTANCE = new IndexEventBus();
    private static RuntimeException constructionException;
    VanillaChronicle chronicle;
    IndexEventSubscriber indexEventSubscriber;
    AtomicLong writes;
    IndexingService indexingService;


    private IndexEventBus() {
        try {
            chronicle = createChronicle();
            writes = new AtomicLong(0);
            indexingService = new IndexingService();
            indexEventSubscriber = new IndexEventSubscriber(indexingService, chronicle.createTailer());
            Gossiper.instance.register(this);
        } catch (Exception e) {
            constructionException = new RuntimeException(e);
        }
    }


    public static IndexEventBus getInstance() {
        if (constructionException != null) throw constructionException;
        else return INSTANCE;
    }

    public void register(RowIndexSupport rowIndexSupport) {
        this.indexingService.register(rowIndexSupport);
        if (StorageService.instance.isInitialized()) {
            indexingService.updateIndexers(rowIndexSupport);
            if (!indexEventSubscriber.started.get()) indexEventSubscriber.start();
        }
    }

    public long publish(ByteBuffer rowKey, ColumnFamily columnFamily) {
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
            if (indexingService.reads.get() >= latest) break;
        }
    }

    private VanillaChronicle createChronicle() {
        String dirName = Options.defaultIndexesDir;
        dirName = dirName + File.separator + "commit-logs";
        File file = new File(dirName);
        boolean createNew = false;
        if (!file.exists()) {
            createNew = file.mkdirs();
        }
        logger.warn("##### Chronicle file path #####[" + file.getPath() + "]. Created new [" + createNew + "]");
        return new VanillaChronicle(file.getPath());
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


}
