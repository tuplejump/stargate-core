package com.tuplejump.stargate.util;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import com.tuplejump.stargate.cassandra.JsonDocumentTest;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;

import java.io.*;
import java.util.List;

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


    private static final JdomParser JDOM_PARSER = new JdomParser();
    private static InputStream is = CQLDataSet.class.getClassLoader().getResourceAsStream("samples/sample.json");

    private static JsonRootNode jsonVal;

    static {
        try {
            jsonVal = JDOM_PARSER.parse(new InputStreamReader(is));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        List<JsonNode> kids = jsonVal.getElements();
        int i = 0;
        File file = new File("src/test/resources/samples/sample-json.cql");
        FileWriter fileWriter = new FileWriter(file);
        for (JsonNode kid : kids) {
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            JsonDocumentTest.formatJsonNode(kid, writer, 0);
            writer.flush();
            String json = sw.toString();
            fileWriter.write("INSERT INTO PERSON_JSON (id,json) values (" + (i++ + 1) + ",'" + json + "');\n");
        }
        fileWriter.flush();
        fileWriter.close();

    }

}
