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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.util.Version;

import java.io.IOException;

/**
 * User: satya
 */
public class JsonTypeFilter extends FilteringTokenFilter {
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    public JsonTypeFilter(Version version, TokenStream input) {
        super(version, input);
    }

    @Override
    protected boolean accept() throws IOException {
        String type = typeAttribute.type();
        if (type == DeweyTokenizer.FIELD || type == DeweyTokenizer.STRING || type == DeweyTokenizer.NUMBER || type == DeweyTokenizer.BOOLEAN)
            return true;
        return false;
    }
}
