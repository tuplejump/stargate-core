package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: satya
 */
public class ClusteringKeyRangeTest extends IndexTestBase {
    String keyspace = "dummyksLang";

    public ClusteringKeyRangeTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        createKS(keyspace);
        createTableAndIndexForRow();
        assertThat(countResults("sample_table", "part=0 AND uid > 4 AND searchName='/.*?AT.*/' ALLOW FILTERING", true), is(4));
        assertThat(countResults("sample_table", "part=0 AND uid < 4 AND searchName='/.*?AT.*/'  ALLOW FILTERING", true), is(1));
        assertThat(countResults("sample_table", "part=0 AND searchName = '/.*?CT.*/'", true), is(5));

    }

    private void createTableAndIndexForRow() {
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE sample_table (part int,uid int,otherid int,othername varchar,searchName varchar,PRIMARY KEY (part, uid,otherid,searchName));");

        getSession().execute("CREATE CUSTOM INDEX sample_table_searchName_key ON sample_table(searchName) USING 'com.tuplejump.stargate.cas.PerRowIndex';");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 1,  1, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 2,  2, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 3,  3, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 4,  4, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 5,  5, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 6,  6, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 7,  7, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 8,  8, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 9,  9, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 10, 10, 'CATV', 'CATV')");
    }
}