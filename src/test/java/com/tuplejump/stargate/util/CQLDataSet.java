package com.tuplejump.stargate.util;

import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;

import java.io.InputStream;

/**
 * User: satya
 */
public class CQLDataSet extends ClassPathCQLDataSet {
    public CQLDataSet(String dataSetLocation, String keyspace) {
        super(dataSetLocation, keyspace);
    }

    @Override
    protected InputStream getInputDataSetLocation(String dataSetLocation) {
        return Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(dataSetLocation);
    }
}
