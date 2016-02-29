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

package com.tuplejump.stargate.lucene;

import org.apache.commons.lang3.ClassUtils;
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
        StandardAnalyzer, WhitespaceAnalyzer, StopAnalyzer, SimpleAnalyzer, KeywordAnalyzer
    }

    public static Analyzer getAnalyzer(String analyzerName) {
        try {
            Analyzers analyzer = Analyzers.valueOf(analyzerName);
            switch (analyzer) {
                case SimpleAnalyzer: {
                    return new SimpleAnalyzer();
                }
                case StandardAnalyzer: {
                    return new StandardAnalyzer();
                }
                case StopAnalyzer: {
                    return new StopAnalyzer();
                }
                case WhitespaceAnalyzer: {
                    return new WhitespaceAnalyzer();
                }
                case KeywordAnalyzer: {
                    return new CaseInsensitiveKeywordAnalyzer();
                }
                default: {
                    return new StandardAnalyzer();
                }
            }
        } catch (IllegalArgumentException e) {
            try {
                Class clazz = ClassUtils.getClass(analyzerName);
                return (Analyzer) clazz.newInstance();
            } catch (ClassNotFoundException e1) {
                throw new IllegalArgumentException("No analyzer class found with name [" + analyzerName + "]");
            } catch (InstantiationException e1) {
                throw new IllegalArgumentException("Could not construct an object of analyzer class with name [" + analyzerName + "]. " +
                        "Unable to find no-args constructor");
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }


}
