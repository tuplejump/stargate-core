package com.tuplejump.stargate.util;

import com.datastax.driver.core.Row;

import java.lang.reflect.Array;
import java.util.*;

public class Record {

    private Map recordDefinition = new HashMap<String, String>();
    private Map<String, Object> record = new HashMap<String, Object>();

    public Record(String[] fields, Object[] values, String[] type) {
        if (fields.length == values.length) {
            for (int i = 0; i < fields.length; i++) {
                recordDefinition.put(fields[i].toLowerCase(), type[i]);
                record.put(fields[i].toLowerCase(), values[i]);
            }
        }
    }

    public Record(Row row, String indexCol) {
        row.getColumnDefinitions().iterator().forEachRemaining(field -> {
            String col = field.getName();
            record.put(col.toLowerCase(), row.getObject(col));
        });
        record.remove(indexCol);
    }

    public String getInsertString() {
        Iterator it = record.entrySet().iterator();
        List<String> fieldList = new ArrayList<String>();
        List<Object> valueList = new ArrayList<Object>();
        List<String> types = new ArrayList<String>();
        while (it.hasNext()) {
            Map.Entry map = (Map.Entry) it.next();
            fieldList.add((String) map.getKey());
            valueList.add(map.getValue());
            types.add((String) recordDefinition.get(map.getKey()));
        }
        return "(" + mkString(fieldList) + ")values(" + mkString(valueList, types) + ");";
    }

    private String mkString(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            result.append(s);
            result.append(",");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    private String mkString(List<Object> list, List<String> recordDefinition) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Object s : list) {
            if (s == null) {
                result.append("null,");
            } else {
                if (recordDefinition.get(i) == "int" || recordDefinition.get(i) == "boolean" || recordDefinition.get(i) == "bigint") {
                    result.append(s.toString());
                    result.append(",");
                } else {
                    result.append("'" + s.toString() + "'");
                    result.append(",");
                }
            }
            i++;
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    public Map getRecord() {
        return record;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Record.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Record other = (Record) obj;
        if ((this.record == null) ? (other.record != null) : !this.record.equals(other.record)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return record.hashCode();
    }

    @Override
    public String toString() {
        return record.toString();
    }
}
