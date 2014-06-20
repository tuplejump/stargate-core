package com.tuplejump.stargate.cassandra;

import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.db.ColumnFamily;

import java.nio.ByteBuffer;

/**
 * User: satya
 */
public interface RowIndexSupport {

    public void indexRow(ByteBuffer rowKey, ColumnFamily cf);

    public String getActualColumnName(ByteBuffer name, CFDefinition cfDef);
}
