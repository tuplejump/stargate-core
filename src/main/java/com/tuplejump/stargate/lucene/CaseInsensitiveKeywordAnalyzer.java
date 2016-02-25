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


    public CaseInsensitiveKeywordAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        KeywordTokenizer source = new KeywordTokenizer();
        LowerCaseFilter filter = new LowerCaseFilter(source);
        return new TokenStreamComponents(source, filter);
    }

}
