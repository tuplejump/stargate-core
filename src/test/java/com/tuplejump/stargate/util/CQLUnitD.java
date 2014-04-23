package com.tuplejump.stargate.util;

import com.datastax.driver.core.Session;
import org.apache.commons.lang.StringUtils;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: satya
 * A CQLUnit Rule which optionally starts a local embedded server.
 */
public class CQLUnitD extends ExternalResource {
    private static final Logger logger = LoggerFactory.getLogger(CQLUnitD.class);


    protected Map<String, Integer> hostsAndPorts;
    protected CQLDataSet dataSet;
    protected String configurationFileName;
    protected Session session;
    protected static String hostIp = "127.0.0.1";
    protected static int port = 9142;
    protected CQLDataLoaderD loader;


    public CQLDataLoaderD getLoader() {
        return loader;
    }

    public Session session() {
        return session;
    }

    public CQLUnitD(CQLDataSet dataSet) {
        this.dataSet = dataSet;
        hostsAndPorts = new HashMap<String, Integer>();
    }

    public CQLUnitD(CQLDataSet dataSet, String configurationFileName) {
        this(dataSet, configurationFileName, hostIp, port);
    }

    public CQLUnitD(CQLDataSet dataSet, String configurationFileName, String host, int port) {
        this(dataSet);
        this.configurationFileName = configurationFileName;
        addHost(host, port);
    }

    public CQLUnitD(CQLDataSet dataSet, Map<String, Integer> hostsAndPorts) {
        this.dataSet = dataSet;
        this.hostsAndPorts = hostsAndPorts;
    }

    protected void addHost(String host, int port) {
        hostsAndPorts.put(host, port);
    }

    @Override
    protected void before() throws Exception {
        /* start an embedded Cassandra */
        if (configurationFileName != null) {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(configurationFileName);
        } else {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        }
        load();
    }

    protected void load() {
        loader = new CQLDataLoaderD(hostsAndPorts);
        session = loader.createSession();
        if (dataSet != null)
            loader.load(dataSet);
    }

    public static CQLUnitD getCQLUnit(com.tuplejump.stargate.util.CQLDataSet ds) {
        CQLUnitD cassandraCQLUnit = null;
        logger.debug("Env prop - cluster - " + System.getProperty("cluster", "false"));
        boolean cluster = Boolean.parseBoolean(System.getProperty("cluster", "false"));
        if (cluster) {
            Properties props = new Properties();
            try {
                props.load(getSeedProps());
                String nodes = props.getProperty("nodes", "EMPTY");
                if (nodes != "EMPTY") {
                    Map<String, Integer> hostsAndPorts = getHostsAndPorts(nodes);
                    logger.debug("**** Starting CQLUnitD in CLUSTER mode **** ");
                    logger.debug("Hosts - " + hostsAndPorts);
                    cassandraCQLUnit = new CQLUnitD(ds, hostsAndPorts);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.debug("**** Starting CQLUnitD in EMBEDDED mode **** ");
            cassandraCQLUnit = new CQLUnitD(ds, "cas.yaml");
        }
        return cassandraCQLUnit;
    }


    public static Map<String, Integer> getHostsAndPorts(String nodes) {
        String[] hostsAndPortsArr = StringUtils.split(nodes, ',');
        Map<String, Integer> hostsAndPorts = new HashMap<String, Integer>();
        for (String hostAndPortStr : hostsAndPortsArr) {
            String[] hostAndPort = StringUtils.split(hostAndPortStr, ':');
            hostsAndPorts.put(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
        }
        return hostsAndPorts;
    }

    public static InputStream getSeedProps() {
        return Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("nodes.properties");
    }

}
