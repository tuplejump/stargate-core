package com.tuplejump.stargate.lucene.json;

import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: satya
 * A collection of lucene fields from a JsonDocument.
 */
public abstract class JsonDocument implements Iterable<Field> {

    protected List<Field> fields = new ArrayList<>();
    protected Properties jsonMapping;
    protected String jsonColName;

    /**
     * @return The fields resulting from the json passed.
     */
    public List<Field> getFields() {
        return fields;
    }

    protected JsonDocument(Properties properties, String jsonColName) {
        this.jsonColName = jsonColName;
        this.jsonMapping = properties.getFields().get(jsonColName);
    }

    @Override
    public Iterator<Field> iterator() {
        return fields.iterator();
    }

    protected Properties getProps(Iterable<String> fieldName) {
        return Options.getProps(jsonMapping, fieldName);
    }

}
