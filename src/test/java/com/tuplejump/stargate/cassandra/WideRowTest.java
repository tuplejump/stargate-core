package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: satya
 */
public class WideRowTest extends IndexTestBase {
    String keyspace = "dummyks4";

    public WideRowTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        try {
            createKS(keyspace);
            createTableAndIndexForRow();
            countResults("TAG2", "", false, true);
            assertThat(countResults("TAG2", "tags = '" + q("tags:hello* AND state:CA") + "'", true), is(12));
            assertThat(countResults("TAG2", "tags = '" + q("tags:hello? AND state:CA") + "'", true), is(12));
            assertThat(countResults("TAG2", "tags = '" + q("tags:hello2 AND state:CA") + "'", true), is(8));
            assertThat(countResults("TAG2", "tags = '" + q("tag2") + "'", true), is(16));

            for (int i = 0; i < 40; i = i + 10) {
                updateTagData("TAG2", (i + 1) + " AND segment =" + i);
            }
            assertThat(countResults("TAG2", "tags = '" + q("h*") + "'", true), is(40));
            assertThat(countResults("TAG2", "tags = '" + q("hello1") + "'", true), is(12));
            for (int i = 0; i < 20; i++) {
                deleteTagData("TAG2", false, i);
            }
            assertThat(countResults("TAG2", "tags = '" + q("hello*") + "'", true), is(16));
        } finally {
            dropTable(keyspace, "TAG2");
            dropKS(keyspace);
        }
    }

    private void createTableAndIndexForRow() {
        String options = "{\"Analyzer\":\"StandardAnalyzer\"," +
                "\"fields\":[\"state\"]," +
                "\"state\":" +
                "{\"striped\":\"true\"}" +
                "}";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG2(key int, tags varchar, state varchar, segment int, PRIMARY KEY(key, segment))");
        int i = 0;
        while (i < 40) {
            if (i == 20) {
                //getSession().execute("CREATE CUSTOM INDEX tagsandstate ON TAG2(tags) WITH options = { 'class': 'com.tuplejump.stargate.cassandra.PerRowIndex'} ");
                getSession().execute("CREATE CUSTOM INDEX tagsandstate ON TAG2(tags) USING 'com.tuplejump.stargate.cassandra.PerRowIndex' WITH options ={'sg_options':'" + options + "'}");
            }
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 1) + ",'hello1 tag1 lol1', 'CA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 2) + ",'hello1 tag1 lol2', 'LA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 3) + ",'hello1 tag2 lol1', 'NY'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 4) + ",'hello1 tag2 lol2', 'TX'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 5) + ",'hllo3 tag3 lol3',  'TX'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 6) + ",'hello2 tag1 lol1', 'CA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 7) + ",'hello2 tag1 lol2', 'NY'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 8) + ",'hello2 tag2 lol1', 'CA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 9) + ",'hello2 tag2 lol2', 'TX'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 10) + ",'hllo3 tag3 lol3', 'TX'," + i + ")");
            i = i + 10;
        }
        Utils.threadSleep(3000);
    }
}
