package com.tuplejump.stargate.lucene.json.dewey;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * User: satya
 */
public class DeweyIdAnalyzer extends Analyzer {
    Version version;

    public DeweyIdAnalyzer(Version version) {
        this.version = version;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        try {
            DeweyTokenizer source = new DeweyTokenizer(reader);
            JsonTypeFilter payloadFilter = new JsonTypeFilter(version, source);
            DeweyFieldTokenizer sink = new DeweyFieldTokenizer(version, payloadFilter);
            return new TokenStreamComponents(source, sink);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
