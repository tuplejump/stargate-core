package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: satya
 */
public class PerColIndexTest extends IndexTestBase {
    String keyspace = "dummyks3";

    public PerColIndexTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerColumn() throws Exception {
        try {
            createKS(keyspace);
            createTableAndIndexForCol();
            assertThat(countResults("TAG", "tags = 'hello*'", true), is(8));
            assertThat(countResults("TAG", "tags = 'hello1'", true), is(4));
            shouldHandleMultipleIndexes();
            assertThat(countResults("TAG", "tags = 'hello2'", true), is(4));
            assertThat(countResults("TAG", "tags = 'tag2'", true), is(4));
            assertThat(countResults("TAG", "tags = 'lol1'", true), is(4));
            assertThat(countResults("TAG", "tags = '/lol[12]/'", true), is(8));
            assertThat(countResults("TAG", "tags = '[lol1 TO lol2]'", true), is(8));
            assertThat(countResults("TAG", "tags = '\"hello1\" OR \"lol2\"'", true), is(6));
            updateTagData("TAG", "'1'");
            assertThat(countResults("TAG", "tags = 'hello2'", true), is(5));
            assertThat(countResults("TAG", "tags = 'hello1'", true), is(3));
            deleteTagsData("TAG", true);
            assertThat(countResults("TAG", "tags = 'hello*'", true), is(4));
        } finally {
            dropTable(keyspace, "TAG");
            dropKS(keyspace);
        }

    }

    private void shouldHandleMultipleIndexes() {
        assertThat(countResults("TAG", "tags = 'hello1' AND state='CA' ALLOW FILTERING", true), is(1));
        assertThat(countResults("TAG", "tags = 'hello1' AND state='NY' ALLOW FILTERING", true), is(1));
        assertThat(countResults("TAG", "tags = 'hello2' AND state='CA' ALLOW FILTERING", true), is(2));
        assertThat(countResults("TAG", "tags = 'hello*' AND state='TX' ALLOW FILTERING", true), is(2));
    }

    private void createTableAndIndexForCol() {
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG(key varchar primary key, tags varchar, state varchar)");
        //first insert some data
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('1','hello1 tag1 lol1', 'CA')");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('2','hello1 tag1 lol2', 'LA')");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('3','hello1 tag2 lol1', 'NY')");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('4','hello1 tag2 lol2', 'TX')");
        //then create the index. old values should be indexed
        //getSession().execute("CREATE CUSTOM INDEX tagsindex ON TAG(tags) WITH options = { 'class': 'com.tuplejump.stargate.cas.PerColIndex'} ");
        //getSession().execute("CREATE CUSTOM INDEX stateindex ON TAG(state) WITH options = { 'class': 'com.tuplejump.stargate.cas.PerColIndex'} ");

        String options = "{\"Analyzer\":\"StandardAnalyzer\"}";
        getSession().execute("XCREATE INDEX tagsindex ON TAG(tags) WITH options='" + options + "'");
        getSession().execute("CREATE CUSTOM INDEX stateindex ON TAG(state) USING 'com.tuplejump.stargate.cas.PerColIndex'");
        //then add some more data and it should be indexed as well
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('5','hello2 tag1 lol1', 'CA')");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('6','hello2 tag1 lol2', 'NY')");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('7','hello2 tag2 lol1', 'CA')");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state) values ('8','hello2 tag2 lol2', 'TX')");
        Utils.threadSleep(3000);
    }

}
