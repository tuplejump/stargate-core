package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.util.CQLUnitD;
import junit.framework.Assert;
import org.junit.Test;

/**
 * User: satya
 */
public class CollectionIndexTest extends IndexTestBase {
    String keyspace = "dummyksColl";

    public CollectionIndexTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexSetAndList() throws Exception {
        //hack to always create new Index during testing
        try {
            createKS(keyspace);
            createTableAndIndexForRow();
            countResults("TAG2", "", false, false);
            Assert.assertEquals(16, countResults("TAG2", "magic = '" + q("tags", "hello1") + "'", true));
            Assert.assertEquals(28, countResults("TAG2", "magic = '" + q("tags2", "bad") + "'", true));
            Assert.assertEquals(16, countResults("TAG2", "magic = '" + q("tags2", "hot") + "'", true));
            Assert.assertEquals(28, countResults("TAG2", "magic = '" + q("phones.key", "patricia") + "'", true));
            //Assert.assertEquals(28, countResults("TAG2", "magic = '" + q("phones.patricia", "555-4326") + "'", true));
            Assert.assertEquals(20, countResults("TAG2", "magic = '" + mq("phones.value", "555-4326") + "'", true));
            Assert.assertEquals(40, countResults("TAG2", "magic = '" + pfq("phones.value", "555") + "'", true));
            for (int i = 0; i < 20; i++) {
                deleteTagData("TAG2", false, i + 1);
            }
            Assert.assertEquals(8, countResults("TAG2", "magic = '" + q("tags2", "hot") + "'", true));
            Assert.assertEquals(14, countResults("TAG2", "magic = '" + q("tags2", "bad") + "'", true));
        } finally {
            dropTable(keyspace, "TAG2");
            dropKS(keyspace);
        }
    }


    private void createTableAndIndexForRow() {
        String options = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{},\n" +
                "\t\t\"tags2\":{},\n" +
                "\t\t\"phones\":{\"fields\":{\"value\":{\"type\":\"string\"}}}\n" +
                "\t}\n" +
                "}";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG2(key int, tags set<text>, tags2 list<text>, phones map<text,varchar>, magic text, PRIMARY KEY (key))");
        getSession().execute("CREATE CUSTOM INDEX tagsIdx ON TAG2(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
        int i = 0;
        while (i < 40) {
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 1) + ",{'hello1','tag1','lol1'},['ugly','bad','hot'  ],{'patricia':'555-4326','doug':'555-1579'})");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 2) + ",{'hello1','tag1','lol2'},['good','nice','cool'],{'bill':'555-7382','patricia':'555-4346'})");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 3) + ",{'hello1','tag2','lol1'},['bad', 'nice','cool'],{'daniel':'555-0453','jane':'555-8743'  })");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 4) + ",{'hello1','tag2','lol2'},['ugly','bad','hot'  ],{'patricia':'555-4326','doug':'555-1579'})");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 5) + ",{'hllo3 ','tag3','lol3'},['good','nice','cool'],{'bill':'555-7382','patricia':'555-4326'})");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 6) + ",{'hello2','tag1','lol1'},['bad', 'nice','cool'],{'daniel':'555-0453','jane':'555-8743'  })");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 7) + ",{'hello2','tag1','lol2'},['ugly','bad','hot'  ],{'patricia':'555-4326','doug':'555-1579'})");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 8) + ",{'hello2','tag2','lol1'},['good','nice','cool'],{'bill':'555-7382','patricia':'555-4346'})");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values (" + (i + 9) + ",{'hello2','tag2','lol2'},['bad', 'nice','cool'],{'daniel':'555-0453','jane':'555-8743'  })");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,tags2,phones) values(" + (i + 10) + ",{'hllo3 ','tag3','lol3'},['ugly','bad','hot'  ],{'patricia':'555-4326','doug':'555-1579'})");
            i = i + 10;
        }
    }
}
