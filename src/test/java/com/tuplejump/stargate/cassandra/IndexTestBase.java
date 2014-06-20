package com.tuplejump.stargate.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * User: satya
 */
public class IndexTestBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Rule
    public CQLUnitD cassandraCQLUnit;
    protected Random rand = new Random();
    protected String[] states = new String[]{"CA", "LA", "TX", "NY", "CA", "LA", "TX", "NY", "CA", "LA", "TX", "NY", "NONEXISTENT"};


    protected void dropTable(String ksName, String tName) {
        getSession().execute("DROP table " + ksName + "." + tName);
    }

    protected void createKS(String ksName) {
        String q = "CREATE KEYSPACE " + ksName + " WITH replication={'class' : 'SimpleStrategy', 'replication_factor':1}";
        getSession().execute(q);
    }

    protected void dropKS(String ksName) {
        getSession().execute("DROP keyspace " + ksName);
    }

    protected int countResults(String tName, String where, boolean log) {
        return countResults(tName, where, true, log);
    }

    protected int countResults(String tName, String where, boolean hasWhr, boolean log) {
        long before = System.nanoTime();
        String select = "select * from ";
        String query = select + tName + (hasWhr ? (" where " + where) : "") + " ";
        ResultSet result = getSession().execute(query);
        long after = System.nanoTime();
        double taken = (after - before) / 1000000;

        Iterator<Row> iter = result.iterator();
        if (log)
            logger.warn("Search for -" + query + " - results -");
        int count1 = 0;
        while (iter.hasNext()) {
            Row row = iter.next();
            if (!log)
                logger.warn(row.toString());
            count1++;
        }
        System.out.println("Search query[" + query + "] in [" + taken + "] ms - count [" + count1 + "]");
        return count1;
    }


    protected void deleteTagsData(String tName, boolean isString) {
        for (int i = 1; i < 5; i++) {
            deleteTagData(tName, isString, i);
        }
    }

    protected void deleteTagData(String tName, boolean isString, int i) {
        String val = isString ? "'" + i + "'" : i + "";
        getSession().execute("delete from " + tName + " where key = " + val);
    }

    protected void updateTagData(String tName, String key) {
        getSession().execute("update " + tName + " set tags='hello2 tag1 lol1', state='NY' where key =" + key);
    }


    protected Session getSession() {
        return cassandraCQLUnit.session();
    }

    protected String getRandomState() {
        int choice = rand.nextInt(4);
        return states[choice];
    }

    protected String q(String field, String value) {
        String query1 = "{ query:{ type:\"lucene\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String wq(String field, String value) {
        String query1 = "{ query:{ type:\"wildcard\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String pfq(String field, String value) {
        String query1 = "{ query:{ type:\"prefix\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String fq(int maxEdits, String field, String value) {
        String query1 = "{ query:{ type:\"fuzzy\", field:\"%s\", value:\"%s\",max_edits:" + maxEdits + " }}";
        return String.format(query1, field, value);
    }

    protected String mq(String field, String value) {
        String query1 = "{ query:{ type:\"match\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String phq(int slop, String field, String... value) {
        String toReplace = "\"%s\"";
        for (int i = 1; i < value.length; i++) toReplace += ",\"%s\"";
        String query1 = "{ query:{ type:\"phrase\", field:\"%s\", values:[" + toReplace + "] , slop:" + slop + "}}";
        List<Object> args = new ArrayList<>();
        args.add(field);
        for (String val : value) args.add(val);
        Object[] arr = new Object[args.size()];
        args.toArray(arr);
        return String.format(query1, arr);
    }

    protected String q(String value) {
        String query1 = "{ \"query\":{ \"type\":\"lucene\", \"value\":\"%s\" }}";
        return String.format(query1, value);
    }

    protected String bq(String query1, String query2) {
        String query = "{ \"query\":{ \"type\":\"boolean\", \"must\":[%s,%s] }}";
        return String.format(query, query1, query2);
    }

    protected String gtq(String field, String value) {
        String query1 = "{ query:{ type:\"range\", field:\"%s\", lower:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String ltq(String field, String value) {
        String query1 = "{ query:{ type:\"range\", field:\"%s\", upper:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String ltEq(String field, String value) {
        String query1 = "{ query:{ type:\"range\", field:\"%s\", upper:\"%s\",include_upper : true  }}";
        return String.format(query1, field, value);
    }

}
