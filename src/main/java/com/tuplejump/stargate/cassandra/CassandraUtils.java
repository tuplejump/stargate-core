package com.tuplejump.stargate.cassandra;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.ConfigurationLoader;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.YamlConfigurationLoader;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class CassandraUtils {
    private static final Logger logger = LoggerFactory.getLogger(CassandraUtils.class);
    private final static String DEFAULT_CONFIGURATION = "cassandra.yaml";
    private static Config conf;

    public static String[] getDataDirs() throws IOException, ConfigurationException {
        if (conf == null) {
            loadYaml();
        }
        return conf.data_file_directories;
    }


    static URL getStorageConfigURL() throws ConfigurationException {
        String configUrl = System.getProperty("cassandra.config");
        if (configUrl == null)
            configUrl = DEFAULT_CONFIGURATION;

        URL url;
        try {
            url = new URL(configUrl);
            url.openStream().close(); // catches well-formed but bogus URLs
        } catch (Exception e) {
            ClassLoader loader = DatabaseDescriptor.class.getClassLoader();
            url = loader.getResource(configUrl);
            if (url == null)
                throw new ConfigurationException("Cannot locate " + configUrl);
        }

        return url;
    }

    static void loadYaml() throws ConfigurationException, IOException {
        URL url = CassandraUtils.getStorageConfigURL();
        logger.info("Loading settings from " + url);
        String loaderClass = System.getProperty("cassandra.config.loader");
        ConfigurationLoader loader = loaderClass == null
                ? new YamlConfigurationLoader()
                : FBUtilities.<ConfigurationLoader>construct(loaderClass, "configuration loading");
        conf = loader.loadConfig();
        logger.info("Data files directories: " + Arrays.toString(conf.data_file_directories));
    }

}