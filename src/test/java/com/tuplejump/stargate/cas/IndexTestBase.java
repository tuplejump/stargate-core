package com.tuplejump.stargate.cas;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: satya
 */
public class IndexTestBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Rule
    public CQLUnitD cassandraCQLUnit;
    protected Random rand = new Random();
    protected String[] states = new String[]{"CA", "LA", "TX", "NY", "CA", "LA", "TX", "NY", "CA", "LA", "TX", "NY", "NONEXISTENT"};

    protected void assertIndexNotExists(String tName) {
        boolean exception = false;
        try {
            assertThat(countResults(tName, "tags = 'tag2'", true), is(0));
        } catch (Exception e) {
            //expected
            exception = true;
        }
        assertThat(exception, is(true));
    }


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

}
