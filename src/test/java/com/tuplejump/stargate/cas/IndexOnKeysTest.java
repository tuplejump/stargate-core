package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.util.CQLUnitD;
import junit.framework.Assert;
import org.junit.Test;

/**
 * User: satya
 */
public class IndexOnKeysTest extends IndexTestBase {
    String keyspace = "dummyks3Keys";

    public IndexOnKeysTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void indexOnKeysShouldReturnResults() throws Exception {
        try {
            createKS(keyspace);
            createTableAndIndex();
            Assert.assertEquals(3, countResults("TAG", "tags = 'tags:hello? AND state:\"CA\"'", true));
        } finally {
            dropTable(keyspace, "TAG");
            dropKS(keyspace);
        }

    }

    private void createTableAndIndex() {
        String options = "{\"Analyzer\":\"StandardAnalyzer\"," +
                "\"fields\":[\"state\",\"gdp\"]," +
                "\"state\":" +
                "{\"striped\":\"true\"}" +
                "}";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG(key varchar, key1 varchar, state varchar, category varchar,tags varchar, gdp bigint, PRIMARY KEY((key,key1),state,gdp))");
        //first insert some data
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('1','A','hello1 tag1 lol1', 'CA','first', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('2','B','hello1 tag1 lol2', 'LA','first', 4)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('3','C','hello1 tag2 lol1', 'NY','first',2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('4','D','hello1 tag2 lol2', 'TX','first',3)");
        //then create the index. old values should be indexed
        getSession().execute("CREATE CUSTOM INDEX tagsandstate ON TAG(tags) USING 'com.tuplejump.stargate.cas.PerRowIndex' WITH options ={'sg_options':'" + options + "'}");
        //then add some more data and it should be indexed as well
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('5','A','hello2 tag1 lol1', 'CA','second', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('6','B','hello2 tag1 lol2', 'NY','second', 2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('7','C','hello2 tag2 lol1', 'CA','second', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('8','D','hello2 tag2 lol2', 'TX','second',3)");
        Utils.threadSleep(3000);
    }

}
