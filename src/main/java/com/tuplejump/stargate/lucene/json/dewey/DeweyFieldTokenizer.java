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

package com.tuplejump.stargate.lucene.json.dewey;

import com.tuplejump.stargate.lucene.CaseInsensitiveKeywordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;

/**
 * User: satya
 */
public class DeweyFieldTokenizer extends TokenFilter {
    private CharTermAttribute termAtt;
    private TypeAttribute typeAtt;
    private PayloadAttribute payloadAtt;

    boolean increment = false;
    private TokenStream currentStream;
    private CharTermAttribute tokenTermAtt;

    Version version;


    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */
    protected DeweyFieldTokenizer(Version version, TokenStream input) {
        super(input);
        initAttributes();
        this.version = version;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        initAttributes();
    }

    private void initAttributes() {
        termAtt = input.getAttribute(CharTermAttribute.class);
        typeAtt = input.getAttribute(TypeAttribute.class);
        payloadAtt = input.getAttribute(PayloadAttribute.class);
    }

    /**
     * Initialise the attributes of the inner stream used to tokenize the incoming token.
     */
    private void initTokenAttributes() {
        tokenTermAtt = currentStream.addAttribute(CharTermAttribute.class);
    }


    @Override
    public boolean incrementToken() throws IOException {
        do {
            if (!increment) {
                if (!input.incrementToken()) {
                    return false;
                }
                String type = typeAtt.type();
                Analyzer analyzer;
                if (type == DeweyTokenizer.STRING) {
                    analyzer = new SimpleAnalyzer(version);
                } else {
                    analyzer = new CaseInsensitiveKeywordAnalyzer(version);
                }
                currentStream = analyzer.tokenStream("", termAtt.toString());
                currentStream.reset();
                this.initTokenAttributes();
            }
            increment = currentStream.incrementToken();
        } while (!increment);
        this.copyAttributes();
        return true;
    }

    private void copyAttributes() {
        BytesRef payload = payloadAtt.getPayload();
        String type = typeAtt.type();
        input.clearAttributes();
        final int len = tokenTermAtt.length();
        termAtt.copyBuffer(tokenTermAtt.buffer(), 0, len);
        typeAtt.setType(type);
        payloadAtt.setPayload(payload);
    }

}
