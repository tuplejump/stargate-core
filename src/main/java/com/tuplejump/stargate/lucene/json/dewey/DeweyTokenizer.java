package com.tuplejump.stargate.lucene.json.dewey;

import argo.staj.JsonStreamElement;
import argo.staj.JsonStreamElementType;
import argo.staj.StajParser;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.Stack;

/**
 * User: satya
 */
public class DeweyTokenizer extends Tokenizer {
    public static final String FIELD = "FIELD";
    public static final String STRING = "STRING";
    public static final String NUMBER = "NUMBER";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String NULL = "NULL";
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);
    private StajParser js;
    Stack<Integer> levelStack;
    int siblingId;
    JsonStreamElementType lastEvent;

    /**
     * creates a new PatternTokenizer returning tokens from group (-1 for split functionality)
     */
    public DeweyTokenizer(Reader input) throws IOException {
        super(input);
        js = new StajParser(input);
        levelStack = new Stack<>();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        js = new StajParser(input);
        levelStack = new Stack<>();
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        if (!js.hasNext()) return false;
        JsonStreamElement el = js.next();
        JsonStreamElementType evt = el.jsonStreamElementType();
        if (evt == null) return false;
        if (evt == JsonStreamElementType.START_DOCUMENT) {
            lastEvent = evt;
            return true;
        }

        if (evt == JsonStreamElementType.START_OBJECT || evt == JsonStreamElementType.START_ARRAY || evt == JsonStreamElementType.START_FIELD) {
            levelStack.push(siblingId);
            siblingId = 0;
            lastEvent = evt;
            if (!(evt == JsonStreamElementType.START_FIELD))
                return true;
        }
        if (evt == JsonStreamElementType.END_OBJECT || evt == JsonStreamElementType.END_ARRAY || evt == JsonStreamElementType.END_FIELD) {
            siblingId = levelStack.size() > 0 ? levelStack.pop() : 0;
            siblingId++;
            lastEvent = evt;
            return true;
        }
        lastEvent = evt;

        switch (evt) {
            case START_FIELD:
                setTerm(el.text());
                typeAtt.setType(FIELD);
                break;

            case STRING:
                setTerm(el.text());
                typeAtt.setType(STRING);
                break;

            case NUMBER:
                setTerm(el.text());
                typeAtt.setType(NUMBER);
                break;

            case FALSE:
                setTerm("false");
                typeAtt.setType(BOOLEAN);
                break;

            case TRUE:
                setTerm("true");
                typeAtt.setType(BOOLEAN);
                break;

            case NULL:
                setTerm("null");
                typeAtt.setType(NULL);
                break;

            default:
                break;
        }
        //set the dewey id as payload
        payloadAtt.setPayload(new BytesRef(encodeLevel()));

        return true;
    }


    private byte[] encodeLevel() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutput dataOutputBuffer = new DataOutputStream(os);
        int size = levelStack.size() + 1;
        dataOutputBuffer.writeShort(size);
        for (int level : levelStack) {
            dataOutputBuffer.writeInt(level);
        }
        dataOutputBuffer.writeInt(siblingId);
        return os.toByteArray();
    }

    public static CharSequence decodeLevel(byte[] bytes) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        DataInput is = new DataInputStream(new ByteArrayInputStream(bytes));
        int size = is.readShort();
        for (int i = 0; i < size; i++) {
            stringBuffer.append(is.readInt()).append('.');
        }
        return stringBuffer;
    }

    public static void main(String[] args) throws IOException {
        String json = "{\n" +
                "    \"id\": 0,\n" +
                "    \"guid\": \"8416dc9e-6904-4787-93eb-b8038f543a04\",\n" +
                "    \"isActive\": false,\n" +
                "    \"balance\": \"$2,858.04\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 25,\n" +
                "    \"eyeColor\": \"brown\",\n" +
                "    \"name\": \"Audra Lynn\",\n" +
                "    \"gender\": \"female\",\n" +
                "    \"company\": \"PROSELY\",\n" +
                "    \"email\": \"audralynn@prosely.com\",\n" +
                "    \"phone\": \"+1 (911) 417-3322\",\n" +
                "    \"address\": \"602 Blake Avenue, Madaket, Maine, 2123\",\n" +
                "    \"about\": \"Enim dolor aliquip est voluptate sit nostrud ut. Dolore sint excepteur nulla et consequat velit cillum veniam quis ex. Consectetur reprehenderit magna minim excepteur magna laboris est sunt.\\r\\n\",\n" +
                "    \"registered\": \"2014-02-05T16:55:14 -06:-30\",\n" +
                "    \"latitude\": 55.275051,\n" +
                "    \"longitude\": 139.3922,\n" +
                "    \"tags\": [\n" +
                "      \"sit\",\n" +
                "      \"laboris\",\n" +
                "      \"do\",\n" +
                "      \"ad\",\n" +
                "      \"et\",\n" +
                "      \"reprehenderit\",\n" +
                "      \"aliqua\"\n" +
                "    ],\n" +
                "    \"friends\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"Fry Richmond\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Olson Knight\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"Jimenez Dominguez\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Audra Lynn! You have 9 unread messages.\",\n" +
                "    \"favoriteFruit\": \"banana\"\n" +
                "  }";

        DeweyTokenizer tokenizer = new DeweyTokenizer(new StringReader(json));
        JsonTypeFilter payloadFilter = new JsonTypeFilter(Version.LUCENE_48, tokenizer);
        DeweyFieldTokenizer fieldTokenizer = new DeweyFieldTokenizer(Version.LUCENE_48, payloadFilter);

        CharTermAttribute charTermAtt = fieldTokenizer.getAttribute(CharTermAttribute.class);
        TypeAttribute typeAtt = fieldTokenizer.getAttribute(TypeAttribute.class);
        PayloadAttribute payloadAtt = fieldTokenizer.getAttribute(PayloadAttribute.class);

        fieldTokenizer.reset();
        while (fieldTokenizer.incrementToken()) {
            System.out.print(charTermAtt.toString() + "\t");
            System.out.print(typeAtt.type() + "\t");
            BytesRef payload = payloadAtt.getPayload();
            if (payload != null)
                System.out.print(DeweyTokenizer.decodeLevel(payload.bytes) + "\n");
        }


    }


    public void setTerm(String term) {
        char[] chars = term.toCharArray();
        termAtt.copyBuffer(term.toCharArray(), 0, chars.length);
    }
}
