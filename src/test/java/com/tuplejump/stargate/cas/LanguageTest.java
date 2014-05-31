package com.tuplejump.stargate.cas;

import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: satya
 */
public class LanguageTest extends IndexTestBase {
    String keyspace = "dummyksLang";

    public LanguageTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        createKS(keyspace);
        createTableAndIndexForRow();
        assertThat(countResults("sample_table", "part=0 AND uid > 4 AND searchName='/.*?AT.*/'", true), is(4));
        assertThat(countResults("sample_table", "part=0 AND uid < 4 AND searchName='/.*?AT.*/'", true), is(1));
        assertThat(countResults("sample_table", "part=0 AND searchName = '/.*?CT.*/'", true), is(5));

    }

    private void createTableAndIndexForRow() {
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE sample_table (\n" +
                "\tpart int,\n" +
                "\tuid int,\n" +
                "\tothername varchar,\n" +
                "\tsearchName varchar,\n" +
                "\tPRIMARY KEY (part, uid)\n" +
                ");");
        getSession().execute("CREATE CUSTOM INDEX sample_table_searchName_key ON sample_table(searchName) USING 'com.tuplejump.stargate.cas.PerRowIndex';");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 1, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 2, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 3, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 4, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 5, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 6, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 7, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 8, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 9, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, othername, searchName) VALUES (0, 10, 'CATV', 'CATV')");
    }
}