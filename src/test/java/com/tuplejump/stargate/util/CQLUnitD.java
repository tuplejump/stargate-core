package com.tuplejump.stargate.util;

import com.datastax.driver.core.Session;
import org.apache.commons.lang.StringUtils;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
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
        logger.error("Env prop - cluster - " + System.getProperty("cluster", "false"));
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

    public static final ObjectMapper jsonMapper = new ObjectMapper();
    public static final JsonFactory f = new MappingJsonFactory();

    static class LowerCaseKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctx)
                throws IOException {
            return key.toLowerCase();
        }
    }

    static {
        f.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        jsonMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        jsonMapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
        SimpleModule module = new SimpleModule("LowerCaseKeyDeserializer",
                new org.codehaus.jackson.Version(1, 9, 0, null));
        module.addKeyDeserializer(Object.class, new LowerCaseKeyDeserializer());
        module.addKeyDeserializer(Map.class, new LowerCaseKeyDeserializer());
        jsonMapper.registerModule(module);

    }

    private static InputStream is2 = CQLUnitD.class.getClassLoader().getResourceAsStream("sample2.json");

    public static void main(String[] args) throws Exception {
        Person[] persons = jsonMapper.readValue(is2, Person[].class);
        File file = new File("samples/sample-json.cql");
        FileWriter fileWriter = new FileWriter(file);
        for (Person person : persons) {
            fileWriter.write(person.toInsertString() + "\n");
        }
        fileWriter.flush();
        fileWriter.close();
    }


    public static class Person {
        @JsonProperty
        int id;
        @JsonProperty
        boolean isActive;
        @JsonProperty
        String balance;
        @JsonProperty
        int age;
        @JsonProperty
        String eyeColor;
        @JsonProperty
        String name;
        @JsonProperty
        String gender;
        @JsonProperty
        String company;
        @JsonProperty
        String email;
        @JsonProperty
        String phone;
        @JsonProperty
        String address;
        @JsonProperty
        String registered;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean isActive) {
            this.isActive = isActive;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEyeColor() {
            return eyeColor;
        }

        public void setEyeColor(String eyeColor) {
            this.eyeColor = eyeColor;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String toInsertString() {
            return "INSERT INTO PERSON" +
                    "('id','isActive','age','eyeColor','name','gender','company','email','phone','address') VALUES" +
                    "(" + id + "," + isActive + "," + age + "," + "'" + eyeColor + "'," + "'" + name + "'," + "'" + gender + "'," + "'" + company + "'," + "'" + email + "'," + "'" + phone + "'," + "'" + address + "');";
        }

        public String toJsonInsertString() throws Exception {
            return "INSERT INTO PERSON_JSON" +
                    "('id','json') VALUES" +
                    "(" + id + ",'" + jsonMapper.writeValueAsString(this) + "');";
        }

    }
}
