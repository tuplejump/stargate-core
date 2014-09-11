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
