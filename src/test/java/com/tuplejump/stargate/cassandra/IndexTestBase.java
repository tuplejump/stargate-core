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
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.util.CQLUnitD;
import org.apache.lucene.analysis.Analyzer;
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
        getSession().execute("DROP table " + ksName + "." + tName);
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

        Iterator<Row> iter = result.iterator();
        int count1 = 0;
        while (iter.hasNext()) {
            Row row = iter.next();
            if (log)
                System.out.println(row.toString());
            count1++;
        }

        System.out.println("Search query[" + query + "] in [" + taken + "] ms - count [" + count1 + "]");
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

    protected String q(String field, String value) {
        String query1 = "{ query:{ type:\"lucene\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String wq(String field, String value) {
        String query1 = "{ query:{ type:\"wildcard\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String pfq(String field, String value) {
        String query1 = "{ query:{ type:\"prefix\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String fq(int maxEdits, String field, String value) {
        String query1 = "{ query:{ type:\"fuzzy\", field:\"%s\", value:\"%s\",maxEdits:" + maxEdits + " }}";
        return String.format(query1, field, value);
    }

    protected String mq(String field, String value) {
        String query1 = "{ query:{ type:\"match\", field:\"%s\", value:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String phq(int slop, String field, String... value) {
        String toReplace = "\"%s\"";
        for (int i = 1; i < value.length; i++) toReplace += ",\"%s\"";
        String query1 = "{ query:{ type:\"phrase\", field:\"%s\", values:[" + toReplace + "] , slop:" + slop + "}}";
        List<Object> args = new ArrayList<>();
        args.add(field);
        for (String val : value) args.add(val);
        Object[] arr = new Object[args.size()];
        args.toArray(arr);
        return String.format(query1, arr);
    }

    protected String bq(String query1, String query2) {
        String query = "{ \"query\":{ \"type\":\"boolean\", \"must\":[%s,%s] }}";
        return String.format(query, query1, query2);
    }

    protected String gtq(String field, String value) {
        String query1 = "{ query:{ type:\"range\", field:\"%s\", lower:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String ltq(String field, String value) {
        String query1 = "{ query:{ type:\"range\", field:\"%s\", upper:\"%s\" }}";
        return String.format(query1, field, value);
    }

    protected String ltEq(String field, String value) {
        String query1 = "{ query:{ type:\"range\", field:\"%s\", upper:\"%s\",includeUpper : true  }}";
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
            Version luceneV = Version.parseLeniently(Version.LUCENE_48.name());
            Analyzer analyzer = new PerFieldAnalyzerWrapper(options.getAnalyzer(), options.perFieldAnalyzers());
            IndexWriterConfig config = new IndexWriterConfig(luceneV, analyzer);
            try {
                Path path = Files.createTempDirectory(null, fileAttributes);
                file = path.toFile();
                file.deleteOnExit();
                directory = FSDirectory.open(file);
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

}
