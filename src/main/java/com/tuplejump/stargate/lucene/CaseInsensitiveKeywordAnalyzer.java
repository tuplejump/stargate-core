package com.tuplejump.stargate.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * User: satya
 * A special analyzer which does not tokenize the passed strings but converts them to lower case
 */
public class CaseInsensitiveKeywordAnalyzer extends Analyzer {

    Version version;

    public CaseInsensitiveKeywordAnalyzer(Version version) {
        this.version = version;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        KeywordTokenizer source = new KeywordTokenizer(reader);
        LowerCaseFilter filter = new LowerCaseFilter(version, source);
        return new TokenStreamComponents(source, filter);
    }


}
