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
