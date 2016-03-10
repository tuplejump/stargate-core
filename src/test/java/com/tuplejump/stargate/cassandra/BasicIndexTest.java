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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.tuplejump.stargate.StargateMBean;
import com.tuplejump.stargate.util.CQLUnitD;
import com.tuplejump.stargate.util.Record;
import junit.framework.Assert;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;


/**
 * User: satya
 */
public class BasicIndexTest extends IndexTestBase {
    String keyspace = "dummyks4";

    public BasicIndexTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    List<Record> recordsForRow = new ArrayList<Record>();
    List<Record> recordsForRowNulls = new ArrayList<Record>();

    @Test
    public void shouldReportErrorRow() throws Exception {
        //hack to always create new Index during testing
        try {
            createKS(keyspace);
            createTableAndIndexForRow();
            ResultSet rs = getResults("TAG2", "magic = 'test'", true);
            List<Row> rows = rs.all();
            Assert.assertEquals(true, rows.toString().contains("error"));
        } finally {
            dropTable(keyspace, "TAG2");
            dropKS(keyspace);
        }
    }

    @Test
    public void shouldIndexNulls() throws Exception {
        //hack to always create new Index during testing
        try {
            createKS(keyspace);
            createTableAndIndexForRowNulls();

            countResults("TAG_NULL", "", false, true);
            Assert.assertEquals(Arrays.asList(recordsForRowNulls.get(15), recordsForRowNulls.get(35), recordsForRowNulls.get(5),
                    recordsForRowNulls.get(25)), getRecords("TAG_NULL", "magic = '" + q("tags", "tags:hello* AND state:CA") + "'", true, "magic"));

            Assert.assertEquals(Arrays.asList(recordsForRowNulls.get(15), recordsForRowNulls.get(35), recordsForRowNulls.get(5),
                    recordsForRowNulls.get(25)), getRecords("TAG_NULL", "magic = '" + q("tags", "tags:hello? AND state:CA") + "'", true, "magic"));

            Assert.assertEquals(Arrays.asList(recordsForRowNulls.get(15), recordsForRowNulls.get(35),
                    recordsForRowNulls.get(5), recordsForRowNulls.get(25)),
                    getRecords("TAG_NULL", "magic = '" + q("tags", "tags:hello2 AND state:CA") + "'", true, "magic"));

            Assert.assertEquals(Arrays.asList(recordsForRowNulls.get(12), recordsForRowNulls.get(17),
                    recordsForRowNulls.get(18), recordsForRowNulls.get(32), recordsForRowNulls.get(37),
                    recordsForRowNulls.get(38), recordsForRowNulls.get(2), recordsForRowNulls.get(7),
                    recordsForRowNulls.get(8), recordsForRowNulls.get(22), recordsForRowNulls.get(27),
                    recordsForRowNulls.get(28)), getRecords("TAG_NULL", "magic = '" + mq("tags", "tag2") + "'", true, "magic"));
        } finally {
            dropTable(keyspace, "TAG_NULL");
            dropKS(keyspace);
        }
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        try {
            createKS(keyspace);
            createTableAndIndexForRow();
            ResultSet resultSet = getResults("TAG2", "magic = '" + q("tags", "tags:hello* AND state:CA") + "'", true);

            countResults("TAG2", "", false, true);
            Assert.assertEquals(Arrays.asList(recordsForRow.get(10), recordsForRow.get(15), recordsForRow.get(17),
                    recordsForRow.get(30), recordsForRow.get(35), recordsForRow.get(37), recordsForRow.get(0),
                    recordsForRow.get(5), recordsForRow.get(7), recordsForRow.get(20), recordsForRow.get(25),
                    recordsForRow.get(27)), getRecords("TAG2", "magic = '" + q("tags", "tags:hello* AND state:CA") + "'", true, "magic"));

            Assert.assertEquals(Arrays.asList(recordsForRow.get(10), recordsForRow.get(15), recordsForRow.get(17),
                    recordsForRow.get(30), recordsForRow.get(35), recordsForRow.get(37), recordsForRow.get(0),
                    recordsForRow.get(5), recordsForRow.get(7), recordsForRow.get(20), recordsForRow.get(25),
                    recordsForRow.get(27)), getRecords("TAG2", "magic = '" + q("tags", "tags:hello? AND state:CA") + "'", true, "magic"));

            Assert.assertEquals(Arrays.asList(recordsForRow.get(15), recordsForRow.get(17), recordsForRow.get(35),
                    recordsForRow.get(37), recordsForRow.get(5), recordsForRow.get(7), recordsForRow.get(25),
                    recordsForRow.get(27)), getRecords("TAG2", "magic = '" + q("tags", "tags:hello2 AND state:CA") + "'", true, "magic"));

            Assert.assertEquals(Arrays.asList(recordsForRow.get(12), recordsForRow.get(13), recordsForRow.get(17),
                    recordsForRow.get(18), recordsForRow.get(32), recordsForRow.get(33), recordsForRow.get(37),
                    recordsForRow.get(38), recordsForRow.get(2), recordsForRow.get(3), recordsForRow.get(7),
                    recordsForRow.get(8), recordsForRow.get(22), recordsForRow.get(23), recordsForRow.get(27),
                    recordsForRow.get(28)), getRecords("TAG2", "magic = '" + mq("tags", "tag2") + "'", true, "magic"));

            for (int i = 0; i < 40; i = i + 10) {
                updateTagData("TAG2", (i + 1) + " AND segment =" + i);
            }
            String[] fields = {"key", "tags", "state", "segment"};
            String[] fieldsType = {"int", "text", "varchar", "int", "text"};
            Record updated01 = new Record(fields, new Object[]{1, "hello2 tag1 lol1", "NY", 0}, fieldsType);
            Record updated11 = new Record(fields, new Object[]{11, "hello2 tag1 lol1", "NY", 10}, fieldsType);
            Record updated21 = new Record(fields, new Object[]{21, "hello2 tag1 lol1", "NY", 20}, fieldsType);
            Record updated31 = new Record(fields, new Object[]{31, "hello2 tag1 lol1", "NY", 30}, fieldsType);

            Assert.assertEquals(Arrays.asList(updated11, recordsForRow.get(11), recordsForRow.get(12), recordsForRow.get(13),
                    recordsForRow.get(14), recordsForRow.get(15), recordsForRow.get(16), recordsForRow.get(17),
                    recordsForRow.get(18), recordsForRow.get(19), updated31, recordsForRow.get(31),
                    recordsForRow.get(32), recordsForRow.get(33), recordsForRow.get(34), recordsForRow.get(35),
                    recordsForRow.get(36), recordsForRow.get(37), recordsForRow.get(38), recordsForRow.get(39),
                    updated01, recordsForRow.get(1), recordsForRow.get(2), recordsForRow.get(3),
                    recordsForRow.get(4), recordsForRow.get(5), recordsForRow.get(6), recordsForRow.get(7),
                    recordsForRow.get(8), recordsForRow.get(9), updated21, recordsForRow.get(21),
                    recordsForRow.get(22), recordsForRow.get(23), recordsForRow.get(24), recordsForRow.get(25),
                    recordsForRow.get(26), recordsForRow.get(27), recordsForRow.get(28), recordsForRow.get(29)),
                    getRecords("TAG2", "magic = '" + q("tags", "h*") + "'", true, "magic"));

            Assert.assertEquals(getRecords("TAG2", "magic = '" + q("tags", "hello1") + "'", true, "magic"), Arrays.asList(
                    recordsForRow.get(11), recordsForRow.get(12), recordsForRow.get(13), recordsForRow.get(31),
                    recordsForRow.get(32), recordsForRow.get(33), recordsForRow.get(1), recordsForRow.get(2),
                    recordsForRow.get(3), recordsForRow.get(21), recordsForRow.get(22), recordsForRow.get(23)));
            int i = 0;
            while (i < 20) {
                i = i + 10;
                deleteTagData("TAG2", "segment", false, i);
            }
            Assert.assertEquals(Arrays.asList(updated31, recordsForRow.get(31), recordsForRow.get(32), recordsForRow.get(33),
                    recordsForRow.get(35), recordsForRow.get(36), recordsForRow.get(37), recordsForRow.get(38),
                    updated01, recordsForRow.get(1), recordsForRow.get(2), recordsForRow.get(3), recordsForRow.get(5),
                    recordsForRow.get(6), recordsForRow.get(7), recordsForRow.get(8)),
                    getRecords("TAG2", "magic = '" + q("tags", "hello*") + "'", true, "magic"));

            //TODO: Ordering of sorted result
            Assert.assertEquals(5, countResults("TAG2", "magic = '" + q("tags", "hello*", "state") + "' limit 5", true));
            Assert.assertEquals(1, countStarResults("TAG2", "magic = '" + q("tags", "hello*") + "'", true));
            Assert.assertEquals(1, countResults("TAG2", "segment=30 and key=36 AND magic = '" + mq("tags", "tag1") + "'", true));
            Assert.assertEquals(0, countResults("TAG2", "segment=20 and key=36 AND magic = '" + mq("tags", "tag1") + "'", true));
            testJMX();

        } finally

        {
            dropTable(keyspace, "TAG2");
            dropKS(keyspace);
        }

    }

    private void testJMX() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(StargateMBean.MBEAN_NAME);
        Assert.assertEquals(true, mBeanServer.isRegistered(objectName));
        String[] values = (String[]) mBeanServer.invoke(objectName, "allIndexes", new Object[]{}, new String[]{});
        for (String value : values) {
            System.out.println(value);
        }
        String[] shards = (String[]) mBeanServer.invoke(objectName, "indexShards", new Object[]{"tagsandstate"}, new String[]{String.class.getName()});
        for (String shard : shards) {
            System.out.println(shard);
        }
        //Assert.assertEquals(256, shards.length);
        String desc = (String) mBeanServer.invoke(objectName, "describeIndex", new Object[]{"tagsandstate"}, new String[]{String.class.getName()});
        System.out.println(desc);
        Long size = (Long) mBeanServer.invoke(objectName, "indexSize", new Object[]{"tagsandstate"}, new String[]{String.class.getName()});
        System.out.println(size);
        Long liveSize = (Long) mBeanServer.invoke(objectName, "indexLiveSize", new Object[]{"tagsandstate"}, new String[]{String.class.getName()});
        System.out.println(liveSize);
        Long writeGen = (Long) mBeanServer.invoke(objectName, "writeGeneration", new Object[]{}, new String[]{});
        System.out.println(writeGen);
        Long readGen = (Long) mBeanServer.invoke(objectName, "readGeneration", new Object[]{}, new String[]{});
        System.out.println(readGen);
        Assert.assertEquals(true, readGen.equals(writeGen));

    }

    private void createTableAndIndexForRow() throws InterruptedException {
        String[] fields = {"key", "tags", "state", "segment"};
        String[] fieldTypes = {"int", "text", "varchar", "int", "text"};
        String options = "{\n" +
                "\t\"numShards\":1024,\n" +
                "\t\"metaColumn\":true,\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{\"type\":\"text\"},\n" +
                "\t\t\"state\":{\"striped\":\"also\",\"analyzer\":\"org.apache.lucene.analysis.core.KeywordAnalyzer\"}\n" +
                "\t}\n" +
                "}\n";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG2(key int, tags text, state varchar, segment int, magic text, PRIMARY KEY(segment, key))");
        int i = 0;
        while (i < 40) {
            if (i == 20) {
                getSession().execute("CREATE CUSTOM INDEX tagsandstate ON TAG2(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
            }
            Record r1 = new Record(fields, new Object[]{(i + 1), "hello1 tag1 lol1", "CA", i}, fieldTypes);
            Record r2 = new Record(fields, new Object[]{(i + 2), "hello1 tag1 lol2", "LA", i}, fieldTypes);
            Record r3 = new Record(fields, new Object[]{(i + 3), "hello1 tag2 lol1", "NY", i}, fieldTypes);
            Record r4 = new Record(fields, new Object[]{(i + 4), "hello1 tag2 lol2", "TX", i}, fieldTypes);
            Record r5 = new Record(fields, new Object[]{(i + 5), "hllo3 tag3 lol3", "TX", i}, fieldTypes);
            Record r6 = new Record(fields, new Object[]{(i + 6), "hello2 tag1 lol1", "CA", i}, fieldTypes);
            Record r7 = new Record(fields, new Object[]{(i + 7), "hello2 tag1 lol2", "NY", i}, fieldTypes);
            Record r8 = new Record(fields, new Object[]{(i + 8), "hello2 tag2 lol1", "CA", i}, fieldTypes);
            Record r9 = new Record(fields, new Object[]{(i + 9), "hello2 tag2 lol2", "TX", i}, fieldTypes);
            Record r10 = new Record(fields, new Object[]{(i + 10), "hllo3 tag3 lol3", "TX", i}, fieldTypes);
            List<Record> tempRecords = Arrays.asList(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10);
            recordsForRow.addAll(tempRecords);
            insertRecords(keyspace, "TAG2", tempRecords);
            i = i + 10;
        }
    }

    private void createTableAndIndexForRowNulls() throws InterruptedException {
        String[] fields = {"key", "tags", "state", "segment"};
        String[] fieldTypes = {"int", "text", "varchar", "int", "text"};
        String options = "{\n" +
                "\t\"numShards\":1024,\n" +
                "\t\"metaColumn\":true,\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{\"type\":\"text\"},\n" +
                "\t\t\"state\":{}\n" +
                "\t}\n" +
                "}\n";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG_NULL(key int, tags text, state varchar, segment int, magic text, PRIMARY KEY(segment, key))");
        int i = 0;
        while (i < 40) {
            if (i == 20) {
                getSession().execute("CREATE CUSTOM INDEX ntagsandstate ON TAG_NULL(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
            }
            Record r1 = new Record(fields, new Object[]{(i + 1), null, "CA", i}, fieldTypes);
            Record r2 = new Record(fields, new Object[]{(i + 2), "hello1 tag1 lol2", null, i}, fieldTypes);
            Record r3 = new Record(fields, new Object[]{(i + 3), "hello1 tag2 lol1", "NY", i}, fieldTypes);
            Record r4 = new Record(fields, new Object[]{(i + 4), null, "TX", i}, fieldTypes);
            Record r5 = new Record(fields, new Object[]{(i + 5), "hllo3 tag3 lol3", "TX", i}, fieldTypes);
            Record r6 = new Record(fields, new Object[]{(i + 6), "hello2 tag1 lol1", "CA", i}, fieldTypes);
            Record r7 = new Record(fields, new Object[]{(i + 7), "hello2 tag1 lol2", "NY", i}, fieldTypes);
            Record r8 = new Record(fields, new Object[]{(i + 8), "hello2 tag2 lol1", null, i}, fieldTypes);
            Record r9 = new Record(fields, new Object[]{(i + 9), "hello2 tag2 lol2", "TX", i}, fieldTypes);
            Record r10 = new Record(fields, new Object[]{(i + 10), "hllo3 tag3 lol3", "TX", i}, fieldTypes);
            List<Record> tempRecords = Arrays.asList(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10);
            recordsForRowNulls.addAll(tempRecords);
            insertRecords(keyspace, "TAG_NULL", tempRecords);
            i = i + 10;
        }
    }
}
