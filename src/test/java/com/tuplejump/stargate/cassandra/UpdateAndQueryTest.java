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
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateAndQueryTest extends IndexTestBase {
    String keyspace = "MY_KEYSPACE";

    public UpdateAndQueryTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    String[] fields = {"id", "isActive", "age", "eyeColor", "name", "gender", "company", "email", "phone", "address"};
    String[] fieldTypes = {"int", "boolean", "int", "varchar", "text", "varchar", "varchar", "varchar", "varchar", "text"};
    List<Record> records = new ArrayList<Record>();

    @Test
    public void checkIndex() {
        createKS(keyspace);
        createTableAndIndex();
        Record r7_new = new Record(fields, new Object[]{7, true, 34, "black", "Avis Mosley", "female", "TETRATREX", "avismosley@tetratrex.com", "+1 (883) 461-3832", "391 Heyward Street, Hayes, Alabama, 5934"}, fieldTypes);
        Record r9_new = new Record(fields, new Object[]{10, false, 38, "blue", "Weaver Carson", "male", "isologix1", "weavercarson@isologix.com", "+1 (916) 566-2681", "560 Hanson Place, Gardners, Puerto Rico, 7821"}, fieldTypes);
        Assert.assertEquals(3, countResults("PERSON", "stargate='" + gtq("age", "30") + "'", true, true));
        getSession().execute("UPDATE PERSON SET company='isologix1' WHERE id=10 AND email='weavercarson@isologix.com'");
        getSession().execute("UPDATE PERSON SET eyeColor='black' WHERE id=7 AND email='avismosley@tetratrex.com'");
        getSession().execute("UPDATE PERSON SET age=27 WHERE id=9 AND email='edwardspatton@mangelica.com'");
        //we set age to 27..this should now result only in 2 docs
        Assert.assertEquals(Arrays.asList(r9_new, r7_new), getRecords("PERSON", "stargate='" + gtq("age", "30") + "'", true, "stargate"));
    }

    private void createTableAndIndex() {
        String tName = "PERSON";
        getSession().execute("USE " + keyspace + ";"); //"int,boolean,int,varchar,text,varchar,varchar,varchar,varchar,text,text
        getSession().execute("CREATE TABLE PERSON (id int,isActive boolean,age int,eyeColor varchar," +
                "name text,gender varchar,company varchar,email varchar,phone varchar,address text,stargate text, PRIMARY KEY (id,email));");
        getSession().execute("CREATE CUSTOM INDEX person_idx ON PERSON(stargate) USING" +
                "'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'{\"fields\":{\"age\":{},\"eyeColor\":{},\"name\":{}," +
                "\"gender\":{},\"company\":{},\"phone\":{},\"address\":{},\"isActive\":{}}}'};");
        Record r1 = new Record(fields, new Object[]{1, false, 29, "green", "Davidson Hurst", "male", "TALKOLA", "davidsonhurst@talkola.com", "+1 (950) 405-2257", "691 Hampton Place, Felt, North Carolina, 8466"}, fieldTypes);
        Record r2 = new Record(fields, new Object[]{2, false, 27, "black", "Maxwell Kemp", "male", "AMTAP", "maxwellkemp@amtap.com", "+1 (800) 495-3822", "466 Kenilworth Place, Fivepointville, Maryland, 6240"}, fieldTypes);
        Record r3 = new Record(fields, new Object[]{3, false, 25, "black", "Cecelia Cain", "female", "MAINELAND", "ceceliacain@maineland.com", "+1 (874) 590-2058", "644 Broome Street, Rutherford, Delaware, 6271"}, fieldTypes);
        Record r4 = new Record(fields, new Object[]{4, true, 28, "green", "Morse Sanders", "male", "APEX", "morsesanders@apex.com", "+1 (857) 427-3391", "786 Division Avenue, Rose, Rhode Island, 4217"}, fieldTypes);
        Record r5 = new Record(fields, new Object[]{5, true, 25, "black", "Fernandez Morse", "male", "OPTICALL", "fernandezmorse@opticall.com", "+1 (911) 442-2649", "116 Suydam Place, Libertytown, Michigan, 2257"}, fieldTypes);
        Record r6 = new Record(fields, new Object[]{6, false, 27, "brown", "Ryan Ross", "male", "ZAPHIRE", "ryanross@zaphire.com", "+1 (843) 423-2420", "804 Erskine Loop, Robinette, Marshall Islands, 9161"}, fieldTypes);
        Record r7 = new Record(fields, new Object[]{7, true, 34, "brown", "Avis Mosley", "female", "TETRATREX", "avismosley@tetratrex.com", "+1 (883) 461-3832", "391 Heyward Street, Hayes, Alabama, 5934"}, fieldTypes);
        Record r8 = new Record(fields, new Object[]{8, false, 29, "black", "Juana Ewing", "female", "REPETWIRE", "juanaewing@repetwire.com", "+1 (809) 410-2791", "510 Lake Avenue, Austinburg, Virgin Islands, 2964"}, fieldTypes);
        Record r9 = new Record(fields, new Object[]{9, false, 33, "brown", "Edwards Patton", "male", "MANGELICA", "edwardspatton@mangelica.com", "+1 (977) 508-2935", "131 Stone Avenue, Cucumber, Minnesota, 4601"}, fieldTypes);
        Record r10 = new Record(fields, new Object[]{10, false, 38, "blue", "Weaver Carson", "male", "ISOLOGIX", "weavercarson@isologix.com", "+1 (916) 566-2681", "560 Hanson Place, Gardners, Puerto Rico, 7821"}, fieldTypes);
        records.addAll(Arrays.asList(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10));
        insertRecords(keyspace, tName, records);
    }
}
