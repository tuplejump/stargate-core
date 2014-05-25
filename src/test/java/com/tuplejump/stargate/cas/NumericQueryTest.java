package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: satya
 */
public class NumericQueryTest extends IndexTestBase {
    String keyspace = "dummyks3N";

    public NumericQueryTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerColumn() throws Exception {
        try {
            createKS(keyspace);
            createTableAndIndexForCol();
            assertThat(countResults("TAG", "gdp = 1", true), is(3));
            assertThat(countResults("TAG", "gdp = 3", true), is(2));
        } finally {
            dropTable(keyspace, "TAG");
            dropKS(keyspace);
        }

    }

    private void createTableAndIndexForCol() {
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG(key varchar primary key, tags varchar, state varchar, gdp int)");
        //first insert some data
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('1','hello1 tag1 lol1', 'CA', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('2','hello1 tag1 lol2', 'LA', 4)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('3','hello1 tag2 lol1', 'NY',2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('4','hello1 tag2 lol2', 'TX',3)");
        //then create the index. old values should be indexed
        //getSession().execute("CREATE CUSTOM INDEX tagsindex ON TAG(tags) WITH options = { 'class': 'com.tuplejump.stargate.cas.PerColIndex'} ");
        //getSession().execute("CREATE CUSTOM INDEX stateindex ON TAG(state) WITH options = { 'class': 'com.tuplejump.stargate.cas.PerColIndex'} ");

        getSession().execute("CREATE CUSTOM INDEX gdpindex ON TAG(gdp) USING 'com.tuplejump.stargate.cas.PerRowIndex'");
        //then add some more data and it should be indexed as well
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('5','hello2 tag1 lol1', 'CA', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('6','hello2 tag1 lol2', 'NY', 2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('7','hello2 tag2 lol1', 'CA', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('8','hello2 tag2 lol2', 'TX',3)");
        Utils.threadSleep(3000);
    }

}
