package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.util.CQLUnitD;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * User: satya
 */
public class NumericIndexOnKeysTest extends IndexTestBase {
    String keyspace = "dummyks3Keys";

    public NumericIndexOnKeysTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void indexOnKeysShouldReturnResults() throws Exception {
        try {
            createKS(keyspace);
            createTableAndIndex();
            Assert.assertEquals(3, countResults("TAG", "magic = '" + q("tags", "tags:hello? AND state:CA") + "'", true));
            final String[] checks = new String[]{
                    "magic = '" + q("tags", "tags:hello? AND gdp:1") + "'",
                    "magic = '" + q("tags", "tags:hello? AND gdp:2") + "'",
                    "magic = '" + q("tags", "tags:hello? AND gdp:3") + "'",
                    "magic = '" + q("tags", "tags:hello? AND gdp:1") + "'",
                    "magic = '" + q("tags", "tags:hello? AND gdp:4") + "'"
            };
            final int[] results = new int[]{3, 2, 2, 3, 1};
            ExecutorService service = Executors.newFixedThreadPool(5);
            List<Callable<Object>> todo = new ArrayList<>();
            for (int i = 0; i < checks.length; i++) {
                final int j = i;
                Callable<Object> callable = new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Assert.assertEquals(results[j], countResults("TAG", checks[j], true));
                        return null;
                    }
                };
                todo.add(callable);
            }
            List<Future<Object>> futures = service.invokeAll(todo);
            int i = 0;
            for (Future<Object> future : futures) {
                future.get();
            }

        } finally {
            dropTable(keyspace, "TAG");
            dropKS(keyspace);
        }

    }

    private void createTableAndIndex() {
        String options = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{},\n" +
                "\t\t\"state\":{},\n" +
                "\t\t\"gdp\":{}\n" +
                "\t}\n" +
                "}\n";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG(key varchar, key1 varchar, state varchar, category varchar,tags varchar, gdp bigint, magic text, PRIMARY KEY((key,key1),state,gdp))");
        //first insert some data
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('1','A','hello1 tag1 lol1', 'CA','first', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('2','B','hello1 tag1 lol2', 'LA','first', 4)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('3','C','hello1 tag2 lol1', 'NY','first',2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('4','D','hello1 tag2 lol2', 'TX','first',3)");
        //then create the index. old values should be indexed
        getSession().execute("CREATE CUSTOM INDEX tagsandstate ON TAG(magic) USING 'com.tuplejump.stargate.cassandra.PerRowIndex' WITH options ={'sg_options':'" + options + "'}");
        //then add some more data and it should be indexed as well
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('5','A','hello2 tag1 lol1', 'CA','second', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('6','B','hello2 tag1 lol2', 'NY','second', 2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('7','C','hello2 tag2 lol1', 'CA','second', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,key1,tags,state,category,gdp) values ('8','D','hello2 tag2 lol2', 'TX','second',3)");
    }

}
