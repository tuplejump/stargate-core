package com.tuplejump.stargate.cassandra;

import argo.format.JsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.lucene.json.JsonDocument;
import com.tuplejump.stargate.lucene.json.StreamingJsonDocument;
import com.tuplejump.stargate.util.CQLUnitD;
import junit.framework.Assert;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * User: satya
 */
public class JsonDocumentTest extends IndexTestBase {

    String keyspace = "dummyksJSON";
    private final JsonFormatter.FieldSorter fieldSorter = new JsonFormatter.FieldSorter() {
        public List<JsonField> sort(List<JsonField> unsorted) {
            return unsorted;
        }
    };

    private static final JdomParser JDOM_PARSER = new JdomParser();
    private static InputStream is = JsonDocumentTest.class.getClassLoader().getResourceAsStream("sample.json");
    private static JsonRootNode jsonVal;

    static {
        try {
            jsonVal = JDOM_PARSER.parse(new InputStreamReader(is));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonDocumentTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexJson() throws Exception {
        String mappingStr = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"jsonCol\":{\n" +
                "\t\t\t\"type\":\"object\",\n" +
                "\t\t\t\"fields\":{\n" +
                "\t\t\t\t\"name\":{\"type\":\"text\"},\n" +
                "\t\t\t\t\"age\":{\"type\":\"integer\"},\n" +
                "\t\t\t\t\"address\":{\"type\":\"text\",\"indexOptions\":\"DOCS_AND_FREQS_AND_POSITIONS\"},\n" +
                "\t\t\t\t\"friends\":{\n" +
                "\t\t\t\t\t\"fields\":{\n" +
                "\t\t\t\t\t\t\"name\":{\"type\":\"string\"}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        createKS(keyspace);
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE JSON_SAMPLE (key int primary key, jsonCol varchar,lucene text)");
        getSession().execute("CREATE CUSTOM INDEX jsonIndex ON JSON_SAMPLE(lucene) USING 'com.tuplejump.stargate.cassandra.PerRowIndex' WITH options ={'sg_options':'" + mappingStr + "'}");
        List<JsonNode> kids = jsonVal.getElements();
        int i = 0;
        for (JsonNode kid : kids) {
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            formatJsonNode(kid, writer, 0);
            writer.flush();
            String json = sw.toString();
            getSession().execute("insert into " + keyspace + ".JSON_SAMPLE (key,jsonCol) values (" + (i++ + 1) + ",'" + json + "')");
        }
        Assert.assertEquals(5, countResults("JSON_SAMPLE", "", false, false));
        Assert.assertEquals(1, countResults("JSON_SAMPLE", "lucene = '" + q("age", "40") + "'", true));
        Assert.assertEquals(2, countResults("JSON_SAMPLE", "lucene = '" + q("tags", "good") + "'", true));
        Assert.assertEquals(3, countResults("JSON_SAMPLE", "lucene = '" + q("tags", "bad") + "'", true));
        Assert.assertEquals(1, countResults("JSON_SAMPLE", "lucene = '" + q("name", "casey*") + "'", true));
        Assert.assertEquals(1, countResults("JSON_SAMPLE", "lucene = '" + wq("name", "casey*") + "'", true));
        Assert.assertEquals(4, countResults("JSON_SAMPLE", "lucene = '" + q("friends.name", "casey*") + "'", true));
        Assert.assertEquals(4, countResults("JSON_SAMPLE", "lucene = '" + mq("friends.name", "Casey Stone") + "'", true));
        Assert.assertEquals(5, countResults("JSON_SAMPLE", "lucene = '" + pfq("friends.name", "ca") + "'", true));
        Assert.assertEquals(1, countResults("JSON_SAMPLE", "lucene = '" + mq("friends.name", "robyn wynn") + "'", true));
        Assert.assertEquals(1, countResults("JSON_SAMPLE", "lucene = '" + fq(1, "friends.name", "robin wynn") + "'", true));
        Assert.assertEquals(1, countResults("JSON_SAMPLE", "lucene = '" + phq(1, "address", "Court", "Hawaii") + "'", true));
    }


    @Test
    public void shouldParseJsonAndGetFields() throws Exception {
        Properties jsonColProps = new Properties();
        jsonColProps.setType(Properties.Type.object);
        Properties ageProps = new Properties();
        ageProps.setType(Properties.Type.integer);
        //json fields mapping
        jsonColProps.setFields(Collections.singletonMap("age", ageProps));
        Properties rootProps = new Properties();
        rootProps.setFields(Collections.singletonMap("jsoncol", jsonColProps));


        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        //formatJsonNode(kid, writer, 0);
        formatJsonNode(jsonVal, writer, 0);
        writer.flush();
        String json = sw.toString();
        JsonDocument jsonDocument = new StreamingJsonDocument(json, rootProps, "jsoncol");
        List<Field> fields = jsonDocument.getFields();
        System.out.println(fields);
        System.out.println("Overall fields:" + fields.size());
        Assert.assertEquals(75, fields.size());
        Assert.assertEquals(5, numberOfFieldsWithKey("name", fields));
        Assert.assertEquals(15, numberOfFieldsWithPrefix("friends", fields));
        Assert.assertEquals(15, numberOfFieldsWithKey("friends.name", fields));
        Assert.assertEquals(15, numberOfFieldsWithKey("tags", fields));
        Assert.assertEquals(5, numberOfFieldsWithNumericType(FieldType.NumericType.INT, fields));
        MemIndex memIndex = new MemIndex(rootProps);
        List<JsonNode> kids = jsonVal.getElements();
        for (JsonNode kid : kids) {
            StringWriter kidSWriter = new StringWriter();
            PrintWriter kidWriter = new PrintWriter(kidSWriter);
            formatJsonNode(kid, kidWriter, 0);
            kidWriter.flush();
            String kidJson = kidSWriter.toString();
            JsonDocument kidDoc = new StreamingJsonDocument(kidJson, rootProps, "jsoncol");
            memIndex.add(kidDoc.getFields());
        }
        Assert.assertEquals(1, memIndex.hits("age:40", "jsoncol"));
        Assert.assertEquals(2, memIndex.hits("eyeColor:blue", "jsoncol"));
        Assert.assertEquals(2, memIndex.hits("gender:female", "jsoncol"));
    }

    private int numberOfFieldsWithKey(String key, List<Field> fields) {
        int size = 0;
        for (Field field : fields) {
            if (field.name().equals(key)) size++;
        }
        System.out.println("Fields with name[" + key + "]:" + size);
        return size;
    }

    private int numberOfFieldsWithPrefix(String prefix, List<Field> fields) {
        int size = 0;
        for (Field field : fields) {
            if (field.name().startsWith(prefix)) size++;
        }
        System.out.println("Fields with prefix[" + prefix + "]:" + size);
        return size;
    }

    private int numberOfFieldsWithNumericType(FieldType.NumericType fieldType, List<Field> fields) {
        int size = 0;
        for (Field field : fields) {
            if (fieldType.equals(field.fieldType().numericType())) size++;
        }
        System.out.println("Fields with type[" + fieldType + "]:" + size);
        return size;
    }


    private void formatJsonNode(final JsonNode jsonNode, PrintWriter writer, int indent) throws IOException {
        switch (jsonNode.getType()) {
            case ARRAY:
                writer.append('[');
                final Iterator<JsonNode> elements = jsonNode.getElements().iterator();
                while (elements.hasNext()) {
                    final JsonNode node = elements.next();
                    writer.println();
                    addTabs(writer, indent + 1);
                    formatJsonNode(node, writer, indent + 1);
                    if (elements.hasNext()) {
                        writer.append(",");
                    }
                }
                if (!jsonNode.getElements().isEmpty()) {
                    writer.println();
                    addTabs(writer, indent);
                }
                writer.append(']');
                break;
            case OBJECT:
                writer.append('{');
                final Iterator<JsonField> jsonStringNodes = fieldSorter.sort(jsonNode.getFieldList()).iterator();
                while (jsonStringNodes.hasNext()) {
                    final JsonField field = jsonStringNodes.next();
                    writer.println();
                    addTabs(writer, indent + 1);
                    formatJsonNode(field.getName(), writer, indent + 1);
                    writer.append(": ");
                    formatJsonNode(field.getValue(), writer, indent + 1);
                    if (jsonStringNodes.hasNext()) {
                        writer.append(",");
                    }
                }
                if (!jsonNode.getFieldList().isEmpty()) {
                    writer.println();
                    addTabs(writer, indent);
                }
                writer.append('}');
                break;
            case STRING:
                writer.append('"')
                        .append(escapeString(jsonNode.getText()))
                        .append('"');
                break;
            case NUMBER:
                writer.append(jsonNode.getText());
                break;
            case FALSE:
                writer.append("false");
                break;
            case TRUE:
                writer.append("true");
                break;
            case NULL:
                writer.append("null");
                break;
            default:
                throw new RuntimeException("Coding failure in Argo:  Attempt to format a JsonNode of unknown type [" + jsonNode.getType() + "];");
        }
    }

    static String escapeString(String unescapedString) {
        return unescapedString
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    private void addTabs(final PrintWriter writer, final int tabs) {
        for (int i = 0; i < tabs; i++) {
            writer.write('\t');
        }
    }

}
