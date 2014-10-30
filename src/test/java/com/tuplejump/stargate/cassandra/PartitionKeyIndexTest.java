/*
 * Copyright 2014, Tuplejump Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.util.CQLUnitD;
import junit.framework.Assert;
import org.junit.Test;

/**
 * User: satya
 */
public class PartitionKeyIndexTest extends IndexTestBase {

    String keyspace = "dummyksPartKey";

    public PartitionKeyIndexTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        try {
            createKS(keyspace);
            createTableAndIndexForRow();
            countResults("TAG2", "", false, true);
            Assert.assertEquals(12, countResults("TAG2", "magic = '" + q("state", "state:CA") + "'", true));
//            Assert.assertEquals(12, countResults("TAG2", "magic = '" + q("tags", "tags:hello* AND state:CA") + "'", true));
//            String q1 = "{ type:\"wildcard\", field:\"tags\", value:\"hello*\" }";
//            String q2 = "{ type:\"match\", field:\"state\", value:\"CA\" }";
//            Assert.assertEquals(12, countResults("TAG2", "magic = '" + bq(q1, q2) + "'", true));
//            Assert.assertEquals(12, countResults("TAG2", "magic = '" + q("tags", "tags:hello? AND state:CA") + "'", true));
//            Assert.assertEquals(8, countResults("TAG2", "magic = '" + q("tags", "tags:hello2 AND state:CA") + "'", true));
        } finally {
            dropTable(keyspace, "TAG2");
            dropKS(keyspace);
        }
    }

    private void createTableAndIndexForRow() {
        String options = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"state\":{}\n" +
                "\t}\n" +
                "}";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG2(key int, tags varchar, state varchar, magic text, PRIMARY KEY ((key,state)))");
        int i = 0;
        while (i < 40) {
            if (i == 20) {
                getSession().execute("CREATE CUSTOM INDEX tagsandstate ON TAG2(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
            }
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 1) + ",'hello1 tag1 lol1', 'CA')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 1) + ",'hello1 tag1 lol2', 'LA')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 1) + ",'hello1 tag2 lol1', 'NY')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 1) + ",'hello1 tag2 lol2', 'TX')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 2) + ",'hllo3 tag3 lol3', 'TX' )");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 2) + ",'hello2 tag1 lol1', 'CA')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 2) + ",'hello2 tag1 lol2', 'NY')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 3) + ",'hello2 tag2 lol1', 'CA')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 3) + ",'hello2 tag2 lol2', 'TX')");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state) values (" + (i + 4) + ",'hllo3 tag3 lol3', 'TX')");
            i = i + 10;
        }
    }
}
