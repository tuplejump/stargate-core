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
import com.tuplejump.stargate.util.Record;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MultipleIndexesTest extends IndexTestBase {

    public MultipleIndexesTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexAsExpectedInCaseOfMultipleSGIndexes() throws Exception {
        String keyspace1 = "dummyks1",
                keyspace2 = "dummyks2",
                tableName = "TAG_TABLE";
        try {
            createKS(keyspace1);
            createKS(keyspace2);
            List<Record> recordsForKS1 = createTableAndIndex(keyspace1, tableName);
            createTableAndIndex(keyspace2, tableName);

            Assert.assertEquals(Arrays.asList(recordsForKS1.get(15), recordsForKS1.get(17), recordsForKS1.get(35),
                    recordsForKS1.get(37), recordsForKS1.get(5), recordsForKS1.get(7), recordsForKS1.get(25),
                    recordsForKS1.get(27)), getRecords(keyspace1 + "." + tableName, "magic = '" + q("tags", "tags:hello2 AND state:CA") + "'", true, "magic"));
            Assert.assertEquals(Arrays.asList(recordsForKS1.get(15), recordsForKS1.get(17), recordsForKS1.get(35),
                    recordsForKS1.get(37), recordsForKS1.get(5), recordsForKS1.get(7), recordsForKS1.get(25),
                    recordsForKS1.get(27)), getRecords(keyspace2 + "." + tableName, "magic = '" + q("tags", "tags:hello2 AND state:CA") + "'", true, "magic"));

            List<Record> fewMoreRecords = insertAdditionalRecords(keyspace1, tableName);
            insertAdditionalRecords(keyspace2, tableName);

            Assert.assertEquals(fewMoreRecords, getRecords(keyspace1 + "." + tableName, "magic = '" + q("tags", "tags:hello1 AND state:MA") + "'", true, "magic"));
            Assert.assertEquals(fewMoreRecords, getRecords(keyspace2 + "." + tableName, "magic = '" + q("tags", "tags:hello1 AND state:MA") + "'", true, "magic"));
        } finally {
            dropTable(keyspace1, tableName);
            dropTable(keyspace2, tableName);
            dropKS(keyspace1);
            dropKS(keyspace2);
        }
    }

    private List<Record> createTableAndIndex(String keyspace, String tableName) throws InterruptedException {
        String[] fields = {"key", "tags", "state", "segment"};
        String[] fieldTypes = {"int", "text", "varchar", "int", "text"};
        List<Record> records = new ArrayList<Record>();
        String options = "{\n" +
                "\t\"numShards\":1024,\n" +
                "\t\"metaColumn\":true,\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{\"type\":\"text\"},\n" +
                "\t\t\"state\":{\"striped\":\"also\",\"analyzer\":\"KeywordAnalyzer\"}\n" +
                "\t}\n" +
                "}\n";
        getSession().execute("CREATE TABLE " + keyspace + "." + tableName + "(key int, tags text, state varchar, segment int, magic text, PRIMARY KEY(segment, key))");
        int i = 0;
        while (i < 40) {
            if (i == 20) {
                getSession().execute("CREATE CUSTOM INDEX tagsandstate ON " + keyspace + "." + tableName + "(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
            }
            Record r1 = new Record(fields, fieldTypes, new Object[]{(i + 1), "hello1 tag1 lol1", "CA", i});
            Record r2 = new Record(fields, fieldTypes, new Object[]{(i + 2), "hello1 tag1 lol2", "LA", i});
            Record r3 = new Record(fields, fieldTypes, new Object[]{(i + 3), "hello1 tag2 lol1", "NY", i});
            Record r4 = new Record(fields, fieldTypes, new Object[]{(i + 4), "hello1 tag2 lol2", "TX", i});
            Record r5 = new Record(fields, fieldTypes, new Object[]{(i + 5), "hllo3 tag3 lol3", "TX", i});
            Record r6 = new Record(fields, fieldTypes, new Object[]{(i + 6), "hello2 tag1 lol1", "CA", i});
            Record r7 = new Record(fields, fieldTypes, new Object[]{(i + 7), "hello2 tag1 lol2", "NY", i});
            Record r8 = new Record(fields, fieldTypes, new Object[]{(i + 8), "hello2 tag2 lol1", "CA", i});
            Record r9 = new Record(fields, fieldTypes, new Object[]{(i + 9), "hello2 tag2 lol2", "TX", i});
            Record r10 = new Record(fields, fieldTypes, new Object[]{(i + 10), "hllo3 tag3 lol3", "TX", i});
            List<Record> tempRecords = Arrays.asList(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10);
            records.addAll(tempRecords);
            insertRecords(keyspace, tableName, tempRecords);
            i = i + 10;
        }
        return records;
    }

    private List<Record> insertAdditionalRecords(String keyspace, String tableName) throws InterruptedException {
        String[] fields = {"key", "tags", "state", "segment"};
        String[] fieldTypes = {"int", "text", "varchar", "int", "text"};
        Record r1 = new Record(fields, fieldTypes, new Object[]{101, "hello1 tag1 lol1", "MA", 100});
        Record r2 = new Record(fields, fieldTypes, new Object[]{102, "hello1 tag1 lol2", "MA", 100});
        List<Record> records = Arrays.asList(r1, r2);
        insertRecords(keyspace, tableName, records);
        return records;
    }
}
