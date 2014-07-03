package com.tuplejump.stargate.lucene;

import com.tuplejump.stargate.Constants;
import com.tuplejump.stargate.lucene.json.dewey.DeweyIdAnalyzer;
import org.apache.commons.lang3.ArrayUtils;
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

    public static Analyzer getAnalyzer(String analyzerName, Version luceneV) {

        if (ArrayUtils.contains(Constants.Analyzers.values(), analyzerName)) {
            try {
                return (Analyzer) Class.forName(analyzerName).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            switch (Constants.Analyzers.valueOf(analyzerName)) {
                case JsonAnalyzer: {
                    return new DeweyIdAnalyzer(luceneV);
                }
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
