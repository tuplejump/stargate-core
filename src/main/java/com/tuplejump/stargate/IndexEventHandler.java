package com.tuplejump.stargate;

import com.lmax.disruptor.EventHandler;
import com.tuplejump.stargate.cassandra.RowIndexSupport;
import org.apache.cassandra.db.ColumnFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class IndexEventHandler implements EventHandler<IndexEntryEvent> {
    protected static final Logger logger = LoggerFactory.getLogger(IndexEventHandler.class);

    private final IndexingService indexingService;
    private final long ordinal;
    private final long numberOfConsumers;

    public IndexEventHandler(IndexingService indexingService, final long ordinal, final long numberOfConsumers) {
        this.indexingService = indexingService;
        this.ordinal = ordinal;
        this.numberOfConsumers = numberOfConsumers;
    }

    @Override
    public void onEvent(IndexEntryEvent event, long sequence, boolean endOfBatch) throws Exception {
        if ((sequence % numberOfConsumers) == ordinal) {
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
}
