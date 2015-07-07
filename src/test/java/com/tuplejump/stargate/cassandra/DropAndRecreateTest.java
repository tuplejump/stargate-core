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
public class DropAndRecreateTest extends IndexTestBase {

    String keyspace = "dummyksDropRecreate";

    public DropAndRecreateTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldAllowToRecreateIndex() throws InterruptedException {
        createKS(keyspace);
        createTableAndIndex(false);
        try {
            Assert.assertEquals(8, countResults("TAG", "", false, false));
            Assert.assertEquals(3, countResults("TAG", "category = '" + mq("state", "CA") + "'", true));
            getSession().execute("DROP INDEX dropcreate;");
            createTableAndIndex(true);
            Assert.assertEquals(8, countResults("TAG", "", false, false));
            Assert.assertEquals(3, countResults("TAG", "category = '" + mq("state", "CA") + "'", true));
        } finally {
            dropKS(keyspace);
        }

    }

    private void createTableAndIndex(boolean isRecreate) {
        String options = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{},\n" +
                "\t\t\"state\":{},\n" +
                "\t\t\"gdp\":{}\n" +
                "\t}\n" +
                "}\n";

        String options1 = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{\"type\":\"text\"},\n" +
                "\t\t\"state\":{},\n" +
                "\t\t\"gdp\":{}\n" +
                "\t}\n" +
                "}\n";
        getSession().execute("USE " + keyspace + ";");
        if (!isRecreate) {
            getSession().execute("CREATE TABLE TAG(key varchar, key1 varchar, state varchar, category varchar,tags text, gdp bigint, PRIMARY KEY((key,key1),state))");
        }
        getSession().execute("CREATE CUSTOM INDEX dropcreate ON TAG(category) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + (isRecreate ? options1 : options) + "'}");
        if (!isRecreate) {
            //first insert some data
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('1','A','hello1 tag1 lol1', 'CA','first', 1)");
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('2','B','hello1 tag1 lol2', 'LA','first', 4)");
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('3','C','hello1 tag2 lol1', 'NY','first',2)");
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('4','D','hello1 tag2 lol2', 'TX','first',3)");
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('5','A','hello2 tag1 lol1', 'CA','second', 1)");
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('6','B','hello2 tag1 lol2', 'NY','second', 2)");
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('7','C','hello2 tag2 lol1', 'CA','second', 1)");
            getSession().execute("insert into TAG (key,key1,tags,state,category,gdp) values ('8','D','hello2 tag2 lol2', 'TX','second',null)");
        }
    }
}
