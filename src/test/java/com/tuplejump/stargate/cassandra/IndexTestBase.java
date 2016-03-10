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
import com.datastax.driver.core.Session;
import com.google.common.base.Joiner;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.util.CQLUnitD;
import com.tuplejump.stargate.util.Record;
import junit.framework.Assert;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.compound.hyphenation.TernaryTree;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.SyncFailedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/**
 * User: satya
 */
public class IndexTestBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Rule
    public CQLUnitD cassandraCQLUnit;
    protected Random rand = new Random();
    protected String[] states = new String[]{"CA", "LA", "TX", "NY", "CA", "LA", "TX", "NY", "CA", "LA", "TX", "NY", "NONEXISTENT"};


    protected void dropTable(String ksName, String tName) {
//        getSession().execute("DROP table " + ksName + "." + tName);
    }

    protected void createKS(String ksName) {
        String q = "CREATE KEYSPACE " + ksName + " WITH replication={'class' : 'SimpleStrategy', 'replication_factor':1}";
        getSession().execute(q);
    }

    protected void dropKS(String ksName) {
        getSession().execute("DROP keyspace " + ksName);
    }

    protected int countResults(String tName, String where, boolean log) {
        return countResults(tName, where, true, log);
    }

    protected ResultSet getResults(String tName, String where, boolean hasWhr) {
        String select = "select * from ";
        String query = select + tName + (hasWhr ? (" where " + where) : "") + " ";
        return getSession().execute(query);
    }

    protected int countResults(String tName, String where, boolean hasWhr, boolean log) {
        long before = System.nanoTime();
        String select = "select * from ";
        String query = select + tName + (hasWhr ? (" where " + where) : "") + " ";
        ResultSet result = getSession().execute(query);
        long after = System.nanoTime();
        double taken = (after - before) / 1000000;
        if (log)
            logger.warn("Search for -" + query + " - results -");

        int count1 = printResultSet(log, result);

        System.out.println("Search query[" + query + "] in [" + taken + "] ms - count [" + count1 + "]");
        return count1;
    }

    protected int countStarResults(String tName, String where, boolean log) {
        return countStarResults(tName, where, true, log);
    }

    protected int countStarResults(String tName, String where, boolean hasWhr, boolean log) {
        long before = System.nanoTime();
        String select = "select count(*) from ";
        String query = select + tName + (hasWhr ? (" where " + where) : "") + " ";
        ResultSet result = getSession().execute(query);
        long after = System.nanoTime();
        double taken = (after - before) / 1000000;
        if (log)
            logger.warn("Search for -" + query + " - results -");

        int count1 = printResultSet(log, result);

        System.out.println("Search query[" + query + "] in [" + taken + "] ms - count [" + count1 + "]");
        return count1;
    }

    protected int countSGResults(String magicCol, String tName, String where, boolean log) {
        return countSGResults(magicCol, tName, where, true, log);
    }

    protected int countSGResults(String magicCol, String tName, String where, boolean hasWhr, boolean log) {
        long before = System.nanoTime();
        String select = "select * from ";
        String query = select + tName + (hasWhr ? (" where " + where) : "") + " ";
        ResultSet result = getSession().execute(query);
        long after = System.nanoTime();
        double taken = (after - before) / 1000000;
        if (log)
            logger.warn("Search for -" + query + " - results -");

        int count1 = printResultSet(log, result);

        System.out.println("Search query[" + query + "] in [" + taken + "] ms - count [" + count1 + "]");
        return count1;
    }

    protected int printResultSet(boolean log, ResultSet result) {
        Iterator<Row> iter = result.iterator();
        int count1 = 0;
        while (iter.hasNext()) {
            Row row = iter.next();
            String rowStr = row.toString();
            if (log)
                System.out.println(rowStr);
            Assert.assertFalse(rowStr.indexOf("error") > 0);
            count1++;
        }
        return count1;
    }

    protected void deleteTagData(String tName, String key, boolean isString, int i) {
        String val = isString ? "'" + i + "'" : i + "";
        getSession().execute("delete from " + tName + " where " + key + " = " + val);
    }

    protected void updateTagData(String tName, String key) {
        getSession().execute("update " + tName + " set tags='hello2 tag1 lol1', state='NY' where key =" + key);
    }


    protected Session getSession() {
        return cassandraCQLUnit.session();
    }

    protected String getRandomState() {
        int choice = rand.nextInt(4);
        return states[choice];
    }

    protected String fun(String field, String name, String type, boolean distinct) {
        if (field == null) {
            if (name == null) {
                String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"%s\",distinct:%b}] }";
                return String.format(query1, type, distinct);
            }
            String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"%s\",alias:\"%s\",distinct:%b}] }";
            return String.format(query1, type, name, distinct);

        } else {
            String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"%s\",field:\"%s\",alias:\"%s\",distinct:%b}] }";
            return String.format(query1, type, field, name, distinct);

        }
    }

    protected String gFun(String field, String name, String type, boolean distinct, String groupBy) {
        if (field == null) {
            if (name == null) {
                String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"%s\",distinct:%b}], groupBy:[\"%s\"] }";
                return String.format(query1, type, distinct, groupBy);
            }
            String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"%s\",alias:\"%s\",distinct:%b}], groupBy:[\"%s\"] }";
            return String.format(query1, type, name, distinct, groupBy);
        } else {
            String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"%s\",field:\"%s\",alias:\"%s\",distinct:%b}], groupBy:[\"%s\"]  }";
            return String.format(query1, type, field, name, distinct, groupBy);

        }
    }

    protected String gQuantile(String field, String name, boolean distinct, String groupBy, int compression) {
        if (field == null) {
            if (name == null) {
                String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"quantile\",compression:\"%s\",distinct:%b}], groupBy:[\"%s\"] }";
                return String.format(query1, compression, distinct, groupBy);
            }
            String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"quantile\",compression:\"%s\",alias:\"%s\",distinct:%b}], groupBy:[\"%s\"] }";
            return String.format(query1, compression, name, distinct, groupBy);
        } else {
            String query1 = "function:{ type:\"aggregate\", aggregates:[{type:\"quantile\",compression:\"%s\",field:\"%s\",alias:\"%s\",distinct:%b}], groupBy:[\"%s\"]  }";
            return String.format(query1, compression, field, name, distinct, groupBy);

        }
    }

    protected String cond(String stateName, String type, String field, String value) {
        String query = "{name:\"%s\", condition:{ type:\"%s\", field:\"%s\", value:\"%s\"}}";
        return String.format(query, stateName, type, field, value);
    }

    protected String define(List<String> steps) {
        Joiner joiner = Joiner.on(",\n");
        return "define:[\n" + joiner.join(steps) + "\n]";
    }

    protected String pattern(String[] refs, boolean[] optional, boolean[] repeat) {
        Joiner joiner = Joiner.on(",\n");
        String template = "{ref:\"%s\",optional:\"%s\",repeat:\"%s\"}";
        List<String> steps = new ArrayList<>(refs.length);
        for (int i = 0; i < refs.length; i++) {
            steps.add(String.format(template, refs[i], optional[i], repeat[i]));
        }
        return "pattern:{steps:[\n" + joiner.join(steps) + "\n]}";
    }


    protected String aggregate(String field, String name, String type, boolean distinct, String groupBy) {
        String query1 = "aggregate:{ aggregates:[{type:\"%s\",field:\"%s\",alias:\"%s\",distinct:%b}], groupBy:[\"%s\"]  }";
        return String.format(query1, type, field, name, distinct, groupBy);
    }

    protected String patternAggregate(String definition, String pattern, String aggregate) {
        String query = "{ function:{ type:\"matchPartition\", %s, %s, %s}}";
        return String.format(query, definition, pattern, aggregate);
    }


    protected String funWithFilter(String fun, String field, String value) {
        String query1 = "{ filter:{ type:\"lucene\", field:\"%s\", value:\"%s\" }, %s}";
        return String.format(query1, field, value, fun);
    }

    protected String q(String field, String value, String... sort) {
        List<String> sorts = new ArrayList<>();
        for (String sortField : sort) {
            sorts.add("{field:\"" + sortField + "\"}");
        }
        String sortString = Joiner.on(",").join(sorts);
        String query1 = "{ filter:{ type:\"lucene\", field:\"%s\", value:\"%s\" }," +
                "sort:{ fields:[" + sortString + "]} }";
        return String.format(query1, field, value);
    }

    protected String q(String field, String value) {
        String query1 = "{ filter:{ type:\"lucene\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String wq(String field, String value) {
        String query1 = "{ filter:{ type:\"wildcard\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String pfq(String field, String value) {
        String query1 = "{ filter:{ type:\"prefix\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String fq(int maxEdits, String field, String value) {
        String query1 = "{ filter:{ type:\"fuzzy\", field:\"%s\", value:\"%s\",maxEdits:" + maxEdits + " }}";
        return String.format(query1, field, value);
    }

    protected String mq(String field, String value) {
        String query1 = "{ filter:{ type:\"match\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String phq(int slop, String field, String... value) {
        String toReplace = "\"%s\"";
        for (int i = 1; i < value.length; i++) toReplace += ",\"%s\"";
        String query1 = "{ filter:{ type:\"phrase\", field:\"%s\", values:[" + toReplace + "] , slop:" + slop + "}}";
        List<Object> args = new ArrayList<>();
        args.add(field);
        for (String val : value) args.add(val);
        Object[] arr = new Object[args.size()];
        args.toArray(arr);
        return String.format(query1, arr);
    }

    protected String bq(String query1, String query2) {
        String query = "{ \"filter\":{ \"type\":\"boolean\", \"must\":[%s,%s] }}";
        return String.format(query, query1, query2);
    }

    protected String gtq(String field, String value) {
        String query1 = "{ filter:{ type:\"range\", field:\"%s\", lower:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String gtq(String field, String value, String format) {
        String query1 = "{ filter:{ type:\"range\", field:\"%s\",  lower:\"%s\",format:\"%s\" }}";
        return String.format(query1, field, value, format);
    }

    protected String ltq(String field, String value, String format) {
        String query1 = "{ filter:{ type:\"range\", field:\"%s\", upper:\"%s\",format:\"%s\" }}";
        return String.format(query1, field, value, format);
    }

    protected String ltEq(String field, String value) {
        String query1 = "{ filter:{ type:\"range\", field:\"%s\", upper:\"%s\",includeUpper : true  }}";
        return String.format(query1, field, value);
    }

    static class MemIndex {
        static Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx--x");
        static FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
        File file;
        Directory directory;
        IndexWriter writer;
        StandardQueryParser parser;
        Properties options;
        DirectoryReader reader;
        IndexSearcher searcher;

        public MemIndex(com.tuplejump.stargate.lucene.Properties properties) {
            this.options = properties;
            Analyzer analyzer = new PerFieldAnalyzerWrapper(options.getLuceneAnalyzer(), options.perFieldAnalyzers());
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try {
                Path path = Files.createTempDirectory(null, fileAttributes);
                file = path.toFile();
                file.deleteOnExit();
                directory = FSDirectory.open(file.toPath());
                writer = new IndexWriter(directory, config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            parser = new StandardQueryParser(analyzer);
            parser.setAllowLeadingWildcard(true);
            parser.setNumericConfigMap(properties.getDynamicNumericConfig());
        }

        public void add(Iterable<Field> fields) throws IOException {
            writer.addDocument(fields);
        }

        public int hits(String luceneQuery, String defaultField) throws Exception {
            maybeRefreshSearcher();
            Query query = parser.parse(luceneQuery, defaultField);
            TopDocs docs = searcher.search(query, Integer.MAX_VALUE);
            return docs.totalHits;
        }

        private void maybeRefreshSearcher() throws IOException {
            if (reader == null) {
                reader = DirectoryReader.open(writer, true);
                searcher = new IndexSearcher(reader);
            } else {
                DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
                if (newReader != null) {
                    reader = newReader;
                    searcher = new IndexSearcher(reader);
                }
            }
        }

    }

    private String createEventInsertStmt(String id, String ts, String dim, String measures) {
        String insertStmt = "INSERT INTO event_store (app_id, event_type, base_ts, event_id, event_ts, dimensions, measures) " +
                "VALUES ('39','beacon','2014-04-06 21:30:00+0530','" + id + "','" + ts + "',{" + dim + "},{" + measures + "});";
        return insertStmt;
    }


    protected void createEventStoreSchema(String keyspace) {
        String createTableStmt = "CREATE TABLE IF NOT EXISTS event_store(app_id text, event_type text, base_ts timestamp, event_id text, event_ts timestamp, keys set<text>, dimensions map<text, text>, measures map<text, double>, stargate text, PRIMARY KEY((app_id, event_type, base_ts, event_id)));";
        String createIndexStmt = "CREATE CUSTOM INDEX IF NOT EXISTS events_stargate_idx ON event_store(stargate) USING 'com.tuplejump.stargate.RowIndex' WITH options = {'sg_options':'{\"fields\": { \"app_id\": {}, \"event_type\": {}, \"base_ts\": {}, \"event_id\" : {}, \"event_ts\": {\"striped\":\"also\"}, \"dimensions\": {\"fields\":{\"_value\":{\"striped\":\"also\", \"type\":\"string\"}}}, \"keys\" :{}, \"measures\": {\"fields\":{\"_value\":{\"striped\":\"also\"}}} } }'};";

        createKS(keyspace);
        getSession().execute("USE " + keyspace + ";");
        getSession().execute(createTableStmt);
        getSession().execute(createIndexStmt);

        List<String> ids = Arrays.asList("ec66026f-8c97-4c57-982f-937h94n34Fv6", "ec66026f-8c97-4c57-982f-545G17A25rM0", "ec66026f-8c97-4c57-982f-388q87g92KW4");
        List<String> times = Arrays.asList("2014-05-04 00:06:00+0530", "2014-05-04 00:08:00+0530", "2014-05-04 00:09:00+0530");
        List<String> dimensions = Arrays.asList("'_browser': 'IE'", "'_browser': 'Firefox'", "'_browser': 'Chrome'");
        List<String> measures = Arrays.asList("'connection': 114", "'connection': 207", "'connection': 374");

        for (int i = 0; i < ids.size(); i++) {
            String stmt = createEventInsertStmt(ids.get(i), times.get(i), dimensions.get(i), measures.get(i));
            getSession().execute(stmt);
        }

    }

    public void insertRecord(String keyspace, String tName, Record record) {
        getSession().execute("insert into " + keyspace + "." + tName + record.getInsertString());
    }

    public void insertRecords(String keyspace, String tName, List<Record> records) {
        records.forEach(rec -> {
            insertRecord(keyspace, tName, rec);
        });
    }

    public List<Record> getRecords(String tName, String where, boolean hasWhr, String indexCol) {
        ResultSet resultSet = getResults(tName, where, hasWhr);
        List<Record> fetched = new ArrayList<Record>();
        resultSet.all().iterator().forEachRemaining(row -> {
            Record tempRecord = new Record(row, indexCol);
            fetched.add(tempRecord);
        });
        return fetched;
    }
}
