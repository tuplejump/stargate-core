/*
 * Copyright 2014, Stratio.
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
package com.tuplejump.stargate.parse;

import com.tuplejump.stargate.lucene.Options;
import org.apache.lucene.search.*;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
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

    private final Sorting sorting;

    /**
     * Returns a new {@link Search} composed by the specified querying and filtering conditions.
     *
     * @param queryCondition  The {@link Condition} for querying, maybe {@code null} meaning no querying.
     * @param filterCondition The {@link Condition} for filtering, maybe {@code null} meaning no filtering.
     */
    @JsonCreator
    public Search(@JsonProperty("query") Condition queryCondition,
                  @JsonProperty("filter") Condition filterCondition,
                  @JsonProperty("sort") Sorting sorting) {
        this.queryCondition = queryCondition;
        this.filterCondition = filterCondition;
        this.sorting = sorting;
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
        return queryCondition != null || sorting != null;
    }

    /**
     * Returns the {@link Condition} for querying. Maybe {@code null} meaning no querying.
     *
     * @return The {@link Condition} for querying. Maybe {@code null} meaning no querying.
     */
    public Condition queryCondition() {
        return queryCondition;
    }

    /**
     * Returns the {@link Condition} for filtering. Maybe {@code null} meaning no filtering.
     *
     * @return The {@link Condition} for filtering. Maybe {@code null} meaning no filtering.
     */
    public Condition filterCondition() {
        return filterCondition;
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

    public Sort sort(Options schema) {
        return sorting == null ? null : sorting.sort(schema);
    }

    /**
     * Returns a new {@link Search} from the specified JSON {@code String}.
     *
     * @param json A JSON {@code String} representing a {@link Search}.
     * @return The {@link Search} represented by the specified JSON {@code String}.
     */
    public static Search fromJson(String json) {
        try {
            Search search = jsonMapper.readValue(json, Search.class);
            return search;
        } catch (Exception e) {
            String message = "Cannot parse JSON index expression: " + json;
            logger.error(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    /**
     * Validates this {@link Search} against the specified {@link Options}.
     *
     * @param schema A {@link Options}.
     */
    public void validate(Options schema) throws Exception {
        if (queryCondition != null) {
            queryCondition.query(schema);
        }
        if (filterCondition != null) {
            filterCondition.filter(schema);
        }
        if (sorting != null) {
            sorting.sort(schema);
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

    /**
     * The embedded JSON serializer.
     */
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    static {
        jsonMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        jsonMapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
    }
}
