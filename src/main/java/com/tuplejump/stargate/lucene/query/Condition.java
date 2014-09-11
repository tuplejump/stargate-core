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

package com.tuplejump.stargate.lucene.query;

import com.tuplejump.stargate.lucene.Options;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The abstract base class for queries.
 * <p/>
 * Known subclasses are:
 * <ul>
 * <li> {@link BooleanCondition}
 * <li> {@link LuceneCondition}
 * <li> {@link FuzzyCondition}
 * <li> {@link MatchCondition}
 * <li> {@link PhraseCondition}
 * <li> {@link PrefixCondition}
 * <li> {@link RangeCondition}
 * <li> {@link WildcardCondition}
 * </ul>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = BooleanCondition.class, name = "boolean"),
        @JsonSubTypes.Type(value = FuzzyCondition.class, name = "fuzzy"),
        @JsonSubTypes.Type(value = LuceneCondition.class, name = "lucene"),
        @JsonSubTypes.Type(value = MatchCondition.class, name = "match"),
        @JsonSubTypes.Type(value = RangeCondition.class, name = "range"),
        @JsonSubTypes.Type(value = PhraseCondition.class, name = "phrase"),
        @JsonSubTypes.Type(value = PrefixCondition.class, name = "prefix"),
        @JsonSubTypes.Type(value = RegexpCondition.class, name = "regex"),
        @JsonSubTypes.Type(value = WildcardCondition.class, name = "wildcard"),})
public abstract class Condition {

    protected static final Logger logger = LoggerFactory.getLogger(Condition.class);

    public static final float DEFAULT_BOOST = 1.0f;

    protected final float boost;

    /**
     * @param boost The boost for this query clause. Documents matching this clause will (in addition to the normal
     *              weightings) have their score multiplied by {@code boost}.
     */
    @JsonCreator
    public Condition(@JsonProperty("boost") Float boost) {
        this.boost = boost == null ? DEFAULT_BOOST : boost;
    }

    /**
     * Returns the boost for this clause. Documents matching this clause will (in addition to the normal weightings)
     * have their score multiplied by {@code boost}. The boost is 1.0 by default.
     *
     * @return The boost for this clause.
     */
    public float getBoost() {
        return boost;
    }

    /**
     * Returns the Lucene's {@link Query} representation of this condition.
     *
     * @param schema The schema to be used.
     * @return The Lucene's {@link Query} representation of this condition.
     */
    public abstract Query query(Options schema) throws Exception;

    /**
     * Returns the Lucene's {@link Filter} representation of this condition.
     *
     * @param schema The schema to be used.
     * @return The Lucene's {@link Filter} representation of this condition.
     */
    public Filter filter(Options schema) throws Exception {
        return new QueryWrapperFilter(query(schema));
    }

    protected String analyze(String field, String value, Analyzer analyzer) {
        TokenStream source = null;
        try {
            source = analyzer.tokenStream(field, value);
            source.reset();

            TermToBytesRefAttribute termAtt = source.getAttribute(TermToBytesRefAttribute.class);
            BytesRef bytes = termAtt.getBytesRef();

            if (!source.incrementToken()) {
                return null;
            }
            termAtt.fillBytesRef();
            if (source.incrementToken()) {
                throw new IllegalArgumentException("analyzer returned too many terms for multiTerm term: " + value);
            }
            source.end();
            return BytesRef.deepCopyOf(bytes).utf8ToString();
        } catch (IOException e) {
            throw new RuntimeException("Error analyzing multiTerm term: " + value, e);
        } finally {
            IOUtils.closeWhileHandlingException(source);
        }
    }

}
