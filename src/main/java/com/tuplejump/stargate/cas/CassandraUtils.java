package com.tuplejump.stargate.cas;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.SeedProviderDef;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
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
        InputStream input;
        URL url = CassandraUtils.getStorageConfigURL();
        logger.info("Loading settings from " + url);
        input = url.openStream();
        org.yaml.snakeyaml.constructor.Constructor constructor = new org.yaml.snakeyaml.constructor.Constructor(Config.class);
        TypeDescription seedDesc = new TypeDescription(SeedProviderDef.class);
        seedDesc.putMapPropertyType("parameters", String.class, String.class);
        constructor.addTypeDescription(seedDesc);
        Yaml yaml = new Yaml(new Loader(constructor));
        conf = (Config) yaml.load(input);
        logger.info("Data files directories: " + Arrays.toString(conf.data_file_directories));
    }

}