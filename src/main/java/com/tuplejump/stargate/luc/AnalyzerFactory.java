package com.tuplejump.stargate.luc;

import com.tuplejump.stargate.Constants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import java.util.Map;

import static com.tuplejump.stargate.Constants.ANALYZER;

/**
 * User: satya
 */
public class AnalyzerFactory {

    public static Analyzer getAnalyzer(Map<String, String> options, Version luceneV) {
        String analyzerName = options.get(ANALYZER);
        if (ArrayUtils.contains(Constants.Analyzers.values(), analyzerName)) {
            try {
                return (Analyzer) Class.forName(analyzerName).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            switch (Constants.Analyzers.valueOf(analyzerName)) {
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

}
