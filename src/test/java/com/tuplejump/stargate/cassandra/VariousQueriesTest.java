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
import org.apache.cassandra.service.StorageService;
import org.junit.Test;

/**
 * User: satya
 */
public class VariousQueriesTest extends IndexTestBase {
    String keyspace = "dummyksLang";

    public VariousQueriesTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        createKS(keyspace);
        createTableAndIndexForRow();
        try {
            countResults("sample_table", "magic = '" + funWithFilter(gFun("otherName", "otherName-values", "values", true, "joindate"), "searchName", "/.*?AT.*/") + "'", true);
            Assert.assertEquals(4, countResults("sample_table", "part=0 AND uid > 4 AND magic='" + q("searchName", "/.*?AT.*/") + "' ALLOW FILTERING", true));
            Assert.assertEquals(1, countResults("sample_table", "part=0 AND uid < 4 AND magic='" + q("searchName", "/.*?AT.*/") + "'  ALLOW FILTERING", true));
            Assert.assertEquals(10, countResults("sample_table", "part=0 AND uid > 0 AND magic='" + fq(1, "searchName", "CZTV") + "'  ALLOW FILTERING", true));

            Assert.assertEquals(10, countResults("sample_table", "part=0 AND magic = '" + fq(1, "searchName", "CZTV") + "'", true));
            Assert.assertEquals(10, countResults("sample_table", "part=0 AND magic = '" + q("searchName", "CATV CCTV") + "'", true));

            Assert.assertEquals(5, countResults("sample_table", "part=0 AND magic = '" + q("searchName", "/.*?CT.*/") + "'", true));
            Assert.assertEquals(5, countResults("sample_table", "part=0 AND magic = '" + pfq("searchName", "ca") + "'", true));
            Assert.assertEquals(5, countResults("sample_table", "part=0 AND magic = '" + mq("searchName", "CATV") + "'", true));
            Assert.assertEquals(5, countResults("sample_table", "part=0 AND magic = '" + fq(1, "searchName", "CATA") + "'", true));
            Assert.assertEquals(0, countResults("sample_table", "part=0 AND magic = '" + fq(0, "searchName", "CCTA") + "'", true));
            Assert.assertEquals(1, countResults("sample_table", "part=0 AND magic = '" + phq(0, "searchName", "aaaa", "BBBB") + "'", true));
            Assert.assertEquals(6, countResults("sample_table", "part=0 AND magic = '" + gtq("searchName", "CATV") + "'", true));
            Assert.assertEquals(3, countResults("sample_table", "part=0 AND magic = '" + gtq("otherid", "9") + "'", true));
            Assert.assertEquals(4, countResults("sample_table", "part=0 AND magic = '" + gtq("joindate", "2013-03-01") + "'", true));
            Assert.assertEquals(8, countResults("sample_table", "part=0 AND magic = '" + ltEq("joindate", "2013-03-01") + "'", true));
            Assert.assertEquals(4, countResults("sample_table", "part=0 AND magic = '" + gtq("joindate", "2013-03-01T00:00:00", "dateHourMinuteSecond") + "'", true));
            Assert.assertEquals(8, countResults("sample_table", "part=0 AND magic = '" + ltq("joindate", "2013-03-01T00:00:00", "dateHourMinuteSecond") + "'", true));


            getSession().execute("DELETE FROM sample_table where part=0");
            Assert.assertEquals(0, countResults("sample_table", "part=0", true));
            //this should write things from memory to SStable.
            StorageService.instance.forceKeyspaceFlush(keyspace.toLowerCase(), "sample_table");
            StorageService.instance.forceKeyspaceCleanup(keyspace.toLowerCase(), "sample_table");
            StorageService.instance.forceKeyspaceCompaction(keyspace.toLowerCase(), "sample_table");
            Assert.assertEquals(0, countResults("sample_table", "part=0 AND magic = '" + fq(1, "searchName", "CZTV") + "'", true));
            Assert.assertEquals(0, countResults("sample_table", "part=0 AND magic = '" + q("searchName", "CATV CCTV") + "'", true));
        } finally {
            dropKS(keyspace);
        }
    }

    private void createTableAndIndexForRow() {
        //add idx options with DOCS_AND_FREQS_AND_POSITIONS for phrase queries.
        String options = "{\n" +
                "\t\"metaColumn\":true,\n" +
                "\t\"fields\":{\n" +
                "\t\t\"searchName\":{\"indexOptions\":\"DOCS_AND_FREQS_AND_POSITIONS\"},\n" +
                "\t\t\"otherName\":{},\n" +
                "\t\t\"joindate\":{},\n" +
                "\t\t\"otherid\":{}\n" +
                "\t}\n" +
                "}\n";

        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE sample_table (part int,uid int,otherid int,othername varchar,searchName varchar,joindate timestamp,magic text,PRIMARY KEY (part, uid,otherid,searchName));");

        getSession().execute("CREATE CUSTOM INDEX sample_table_searchName_key ON sample_table(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-02-01+0530',0, 1,  1, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-02-02+0530',0, 2,  2, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-02-03+0530',0, 4,  4, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-02-04+0530',0, 5,  5, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-02-05+0530',0, 8,  8, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-02-06+0530',0, 3,  3, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-03-01+0530',0, 6,  6, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-03-01+0530',0, 9,  9, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-03-02+0530',0, 7,  7, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-03-03+0530',0, 10, 10, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-03-04+0530',0, 11,  11, 'AAAA ', 'AAAA aaaa cccc BBBB')");
        getSession().execute("INSERT INTO sample_table (joindate,part, uid, otherid,othername, searchName) VALUES ('2013-03-05+0530',0, 12,  12, 'AAAA ', 'AAAA bbbb aaaa AAAA BBBB')");

    }
}