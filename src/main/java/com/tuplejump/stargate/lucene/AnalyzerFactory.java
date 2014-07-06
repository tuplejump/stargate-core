package com.tuplejump.stargate.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 * User: satya
 * A factory for making analyzer from the name given in the mapping.
 */
public class AnalyzerFactory {
    public enum Analyzers {
        StandardAnalyzer, WhitespaceAnalyzer, StopAnalyzer, SimpleAnalyzer, KeywordAnalyzer, JsonAnalyzer

    }

    public static Analyzer getAnalyzer(String analyzerName, Version luceneV) {

        switch (Analyzers.valueOf(analyzerName)) {
            case SimpleAnalyzer: {
                return new SimpleAnalyzer(luceneV);
            }
            case StandardAnalyzer: {
                return new StandardAnalyzer(luceneV);
            }
            case StopAnalyzer: {
                return new StopAnalyzer(luceneV);
            }
            case WhitespaceAnalyzer: {
                return new WhitespaceAnalyzer(luceneV);
            }
            case KeywordAnalyzer: {
                return new CaseInsensitiveKeywordAnalyzer(luceneV);
            }
            default: {
                return new StandardAnalyzer(luceneV);
            }
        }
    }


}
