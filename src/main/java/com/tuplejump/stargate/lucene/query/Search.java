/*
 * Copyright 2014, Stratio.
 * Modification and adapations - Copyright 2014, Tuplejump Inc.
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
import com.tuplejump.stargate.lucene.query.function.Function;
import com.tuplejump.stargate.lucene.query.function.NoOp;
import org.apache.lucene.search.*;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing an Lucene's index search. It is formed by an optional querying {@link Condition} and an optional
 * filtering {@link Condition}. It can be translated to a Lucene's {@link Query} using a {@link com.tuplejump.stargate.lucene.Options}.
 */
public class Search {
    private static final Logger logger = LoggerFactory.getLogger(Search.class);
    /**
     * The querying condition
     */
    private final Condition queryCondition;

    /**
     * The filtering condition
     */
    private final Condition filterCondition;


    /**
     * The sort to apply
     */
    private final Sort sort;


    private final Function function;

    private final boolean showScore;

    /**
     * Returns a new {@link Search} composed by the specified querying and filtering conditions.
     *
     * @param queryCondition  The {@link Condition} for querying, maybe {@code null} meaning no querying.
     * @param filterCondition The {@link Condition} for filtering, maybe {@code null} meaning no filtering.
     */
    @JsonCreator
    public Search(@JsonProperty("query") Condition queryCondition,
                  @JsonProperty("filter") Condition filterCondition,
                  @JsonProperty("sort") Sort sort, @JsonProperty("function") Function function, @JsonProperty("score") boolean showScore) {
        this.queryCondition = queryCondition;
        this.filterCondition = filterCondition;
        this.sort = sort;
        if (function == null) this.function = new NoOp();
        else this.function = function;
        this.showScore = showScore;
    }

    /**
     * Returns {@code true} if the results must be ordered by relevance. If {@code false}, then the results are sorted
     * by the natural Cassandra's order. Results must be ordered by relevance if the querying condition is not {code
     * null}.
     * <p/>
     * Relevance is used when the query condition is set, and it is not used when only the filter condition is set.
     *
     * @return {@code true} if the results must be ordered by relevance. If {@code false}, then the results must be
     * sorted by the natural Cassandra's order.
     */
    public boolean usesSorting() {
        return queryCondition != null || sort != null;
    }

    public Function function(Options schema) throws Exception {
        return this.function;
    }


    public boolean isShowScore() {
        return showScore;
    }

    /**
     * Returns the Lucene's {@link Query} representation of this search. This {@link Query} include both the querying
     * and filtering {@link Condition}s. If none of them is set, then a {@link MatchAllDocsQuery} is returned.
     *
     * @param schema
     * @return The Lucene's {@link Query} representation of this search.
     */
    public Query query(Options schema) throws Exception {
        Query query = queryCondition == null ? null : queryCondition.query(schema);
        Filter filter = filterCondition == null ? null : filterCondition.filter(schema);
        if (query == null && filter == null) {
            return new MatchAllDocsQuery();
        } else if (query != null && filter == null) {
            return query;
        } else if (query == null && filter != null) {
            return new ConstantScoreQuery(filter);
        } else {
            return new FilteredQuery(query, filter);
        }
    }

    public org.apache.lucene.search.SortField[] sort(Options schema) {
        return sort == null ? null : sort.sort(schema);
    }

    /**
     * Returns a new {@link Search} from the specified JSON {@code String}.
     *
     * @param json A JSON {@code String} representing a {@link Search}.
     * @return The {@link Search} represented by the specified JSON {@code String}.
     */
    public static Search fromJson(String json) {
        try {
            return Options.inputMapper.readValue(json, Search.class);
        } catch (Exception e) {
            String message = "Cannot parse JSON index expression: " + json;
            logger.error(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Search [query=");
        builder.append(queryCondition);
        builder.append(", filter=");
        builder.append(filterCondition);
        builder.append("]");
        return builder.toString();
    }

}
