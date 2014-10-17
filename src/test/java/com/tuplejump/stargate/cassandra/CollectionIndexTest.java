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
            Assert.assertEquals(28, countResults("TAG2", "magic = '" + q("phones._key", "patricia") + "'", true));
            Assert.assertEquals(28, countResults("TAG2", "magic = '" + q("phones.patricia", "555-4326") + "'", true));
            Assert.assertEquals(20, countResults("TAG2", "magic = '" + mq("phones._value", "555-4326") + "'", true));
            Assert.assertEquals(40, countResults("TAG2", "magic = '" + pfq("phones._value", "555") + "'", true));
            for (int i = 0; i < 20; i++) {
                deleteTagData("TAG2", "key", false, i + 1);
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
                "\t\t\"phones\":{\"fields\":{\"_value\":{\"type\":\"string\"}}}\n" +
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

    @Test
    public void shouldAggregateCollections() throws Exception {
        try {
            createKS(keyspace);
            createTableAndIndexForRowAgg(false);
            countResults("CUBE", "", false, false);
            Assert.assertEquals(12, countResults("CUBE", "magic = '" + q("dimensions._browser", "Chrome") + "'", true));
            Assert.assertEquals(24, countResults("CUBE", "magic = '" + q("dimensions._os", "Windows") + "'", true));

            countSGResults("magic", "CUBE", "magic = '{" + gFun(null, "count*", "count", false, "dimensions._browser") + "}'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.loadTime", "loadTime-Count", "count", false, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.loadTime", "loadTime-Count", "count", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.loadTime", "loadTime-Sum", "sum", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.fetchTime", "fetchTime-Max", "max", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.loadTime", "loadTime-Min", "min", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.fetchTime", "fetchTime-Values", "values", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(fun("dimensions._browser", "browser-values", "values", true), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '{" + fun(null, "count*", "count", false) + "}'", true);
        } finally {
            dropTable(keyspace, "CUBE");
            dropKS(keyspace);
        }
    }

    @Test
    public void shouldAggregateCollectionsStriped() throws Exception {
        try {
            createKS(keyspace);
            createTableAndIndexForRowAgg(true);
            countResults("CUBE", "", false, false);
            Assert.assertEquals(12, countResults("CUBE", "magic = '" + q("dimensions._browser", "Chrome") + "'", true));
            Assert.assertEquals(24, countResults("CUBE", "magic = '" + q("dimensions._os", "Windows") + "'", true));
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.loadTime", "loadTime-Sum", "count", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.loadTime", "loadTime-Sum", "sum", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.fetchTime", "fetchTime-Max", "max", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.loadTime", "loadTime-Min", "min", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(gFun("metrics.fetchTime", "fetchTime-Values", "values", true, "dimensions._browser"), "dimensions._os", "Windows") + "'", true);
            countSGResults("magic", "CUBE", "magic = '" + funWithFilter(fun("dimensions._browser", "browser-values", "values", true), "dimensions._os", "Windows") + "'", true);
        } finally {
            dropTable(keyspace, "CUBE");
            dropKS(keyspace);
        }
    }


    private void createTableAndIndexForRowAgg(boolean striped) {
        String optionsStr = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"metrics\":{\"fields\":{\"_value\":{striped:\"only\"}}},\n" +
                "\t\t\"dimensions\":{\"fields\":{\"_value\":{striped:\"also\",\"type\":\"string\"}}}\n" +
                "\t}\n" +
                "}";
        String optionsNor = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"metrics\":{},\n" +
                "\t\t\"dimensions\":{\"fields\":{\"_value\":{\"type\":\"string\"}}}\n" +
                "\t}\n" +
                "}";

        String options = striped ? optionsStr : optionsNor;

        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE CUBE(key int, dimensions map<text,varchar>,metrics map<text,decimal>, magic text, PRIMARY KEY (key))");
        getSession().execute("CREATE CUSTOM INDEX tagsIdx ON CUBE(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
        int i = 0;
        while (i < 40) {
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 1) + ",{'loadTime':20,'fetchTime':1},{'_browser':'Chrome','_os':'Windows'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 2) + ",{'loadTime':15,'fetchTime':3},{'_browser':'Firefox','_os':'Windows'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 3) + ",{'loadTime':10,'fetchTime':5},{'_browser':'UCBrowser','_os':'Windows'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 4) + ",{'loadTime':5,'fetchTime':7},{'_browser':'Opera','_os':'Windows'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 5) + ",{'loadTime':10,'fetchTime':2.5},{'_browser':'Chrome','_os':'Linux'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 6) + ",{'loadTime':25,'fetchTime':4},{'_browser':'Firefox','_os':'Linux'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 7) + ",{'loadTime':20,'fetchTime':6},{'_browser':'UCBrowser','_os':'Windows'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 8) + ",{'loadTime':30,'fetchTime':8},{'_browser':'Opera','_os':'Windows'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values (" + (i + 9) + ",{'loadTime':15,'fetchTime':9.5},{'_browser':'Chrome','_os':'Mac'})");
            getSession().execute("insert into " + keyspace + ".CUBE (key,metrics,dimensions) values(" + (i + 10) + ",{'loadTime':5,'fetchTime':1.5},{'_browser':'Safari','_os':'Mac'})");
            i = i + 10;
        }
    }
}
