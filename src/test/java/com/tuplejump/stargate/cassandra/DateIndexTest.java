package com.tuplejump.stargate.cassandra;

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
      createKS(keyspace);

      String createTableStmt = "CREATE TABLE IF NOT EXISTS event_store(app_id text, event_type text, base_ts timestamp, event_id text, event_ts timestamp, keys set<text>, dimensions map<text, text>, measures map<text, double>, stargate text, PRIMARY KEY((app_id, event_type, base_ts, event_id)));";
      String createIndexStmt = "CREATE CUSTOM INDEX IF NOT EXISTS events_stargate_idx ON event_store(stargate) USING 'com.tuplejump.stargate.RowIndex' WITH options = {'sg_options':'{\"fields\": { \"app_id\": {}, \"event_type\": {}, \"base_ts\": {}, \"event_id\" : {}, \"event_ts\": {\"striped\":\"only\"}, \"dimensions\": {\"fields\":{\"_value\":{\"striped\":\"also\", \"type\":\"string\"}}}, \"keys\" :{}, \"measures\": {\"fields\":{\"_value\":{\"striped\":\"also\"}}} } }'};";
      String insertStmt = "INSERT INTO event_store (app_id, event_type, base_ts, event_id, event_ts, dimensions, measures) VALUES ('39','beacon','2014-04-06 21:30:00+0530','ec66026f-8c97-4c57-982f-937h94n34Fv6','2014-05-04 00:06:00+0530',{'_browser': 'IE'},{'cartAmount': 1814.6, 'cartSize': 9});";

      getSession().execute("USE " + keyspace + ";");
      getSession().execute(createTableStmt);
      getSession().execute(createIndexStmt);
      getSession().execute(insertStmt);

      //query that groups by 10mins granularity
      String query = "SELECT stargate FROM hydra_events.event_store WHERE stargate = '{ function:{ type:\"aggregate\", aggregates:[{type:\"count\",field:\"event_id\"}], groupBy:[\"return DateUtils.getTimeByGranularity(event_ts,600000)\"]  }}' ;";
      getSession().execute(query);

    } finally {
      dropKS(keyspace);
    }
  }

}
