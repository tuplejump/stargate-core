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
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A {@link Condition} implementation that matches documents satisfying a Lucene Query Syntax.
 */
public class LuceneCondition extends Condition {

    /**
     * The default field name
     */
    private final String defaultField;

    /**
     * The query value
     */
    private final String query;

    /**
     * Constructor using the field name and the value to be matched.
     *
     * @param boost        The boost for this query clause. Documents matching this clause will (in addition to the normal
     *                     weightings) have their score multiplied by {@code boost}. If {@code null}, then DEFAULT_BOOST}
     *                     is used as default.
     * @param defaultField the default field name.
     * @param query        the Lucene Query Syntax query.
     */
    @JsonCreator
    public LuceneCondition(@JsonProperty("boost") Float boost,
                           @JsonProperty("field") String defaultField,
                           @JsonProperty("value") String query) {
        super(boost);

        this.query = query;
        this.defaultField = defaultField;
    }

    /**
     * Returns the default field name.
     *
     * @return the default field name.
     */
    String getDefaultField(Options schema) {
        return defaultField == null ? schema.defaultField : defaultField.toLowerCase();
    }

    /**
     * Returns the Lucene Query Syntax query.
     *
     * @return the Lucene Query Syntax query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query query(Options schema) {
        if (query == null) {
            throw new IllegalArgumentException("Query statement required");
        }
        try {
            StandardQueryParser parser = new StandardQueryParser(schema.analyzer);
            parser.setNumericConfigMap(schema.numericFieldOptions);
            parser.setAllowLeadingWildcard(true);
            Query luceneQuery = parser.parse(query, getDefaultField(schema));
            luceneQuery.setBoost(boost);
            logger.debug("Lucene query is {}", luceneQuery);
            return luceneQuery;
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing lucene syntax query", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(" [boost=");
        builder.append(boost);
        builder.append(", defaultField=");
        builder.append(defaultField);
        builder.append(", query=");
        builder.append(query);
        builder.append("]");
        return builder.toString();
    }

}