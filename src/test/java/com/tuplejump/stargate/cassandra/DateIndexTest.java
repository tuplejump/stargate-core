package com.tuplejump.stargate.cassandra;

import com.datastax.driver.core.ResultSet;
import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Test;

public class DateIndexTest extends IndexTestBase {
    String keyspace = "hydra";

    public DateIndexTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldWorkForDateColumn() throws Exception {
        try {
            createEventStoreSchema(keyspace);
            //query that groups by 10mins granularity
            String query = "SELECT stargate FROM " + keyspace + ".event_store WHERE stargate = '{ function:{ type:\"aggregate\", aggregates:[{type:\"count\",field:\"event_id\"}], groupBy:[\"return DateUtils.getTimeByGranularity(event_ts,600000)\"],imports:[\"com.tuplejump.stargate\"]  }}' ;";
            ResultSet rows = getSession().execute(query);
            printResultSet(true, rows);

        } finally {
            // dropKS(keyspace);
        }
    }

}
