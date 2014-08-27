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
            Assert.assertEquals(3, countResults("TAG", "state = '" + q("gdp", "1") + "'", true));
            Assert.assertEquals(2, countResults("TAG", "state = '" + q("gdp", "3") + "'", true));
            Assert.assertEquals(3, countResults("TAG", "state = '" + gtq("gdp", "2") + "'", true));
            Assert.assertEquals(5, countResults("TAG", "state = '" + ltEq("gdp", "2") + "'", true));
        } finally {
            dropTable(keyspace, "TAG");
            dropKS(keyspace);
        }

    }

    private void createTableAndIndexForCol() {
        String options = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"gdp\":{\"type\":\"integer\"}\n" +

                "\t}\n" +
                "}\n";

        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG(key varchar primary key, tags varchar, state varchar, gdp int)");
        //first insert some data
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('1','hello1 tag1 lol1', 'CA', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('2','hello1 tag1 lol2', 'LA', 4)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('3','hello1 tag2 lol1', 'NY',2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('4','hello1 tag2 lol2', 'TX',3)");
        //then create the index. old values should be indexed
        getSession().execute("CREATE CUSTOM INDEX gdpindex ON TAG(state) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
        //then add some more data and it should be indexed as well
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('5','hello2 tag1 lol1', 'CA', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('6','hello2 tag1 lol2', 'NY', 2)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('7','hello2 tag2 lol1', 'CA', 1)");
        getSession().execute("insert into " + keyspace + ".TAG (key,tags,state,gdp) values ('8','hello2 tag2 lol2', 'TX',3)");
    }

}
