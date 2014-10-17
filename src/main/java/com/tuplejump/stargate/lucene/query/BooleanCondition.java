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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * A {@link Condition} that matches documents matching boolean combinations of other queries, e.g.
 * {@link MatchCondition}s, {@link RangeCondition}s or other {@link BooleanCondition}s.
 */
public class BooleanCondition extends Condition {

    private List<Condition> must;

    private List<Condition> should;

    private List<Condition> not;

    /**
     * Returns a new {@link BooleanCondition} compound by the specified {@link Condition}s.
     *
     * @param boost  The boost for this query clause. Documents matching this clause will (in addition to the normal
     *               weightings) have their score multiplied by {@code boost}.
     * @param must   the mandatory {@link Condition}s.
     * @param should the optional {@link Condition}s.
     * @param not    the mandatory not {@link Condition}s.
     */
    @JsonCreator
    public BooleanCondition(@JsonProperty("boost") Float boost,
                            @JsonProperty("must") List<Condition> must,
                            @JsonProperty("should") List<Condition> should,
                            @JsonProperty("not") List<Condition> not) {
        super(boost);
        this.must = must == null ? new LinkedList<Condition>() : must;
        this.should = should == null ? new LinkedList<Condition>() : should;
        this.not = not == null ? new LinkedList<Condition>() : not;
    }

    /**
     * Returns the mandatory {@link Condition}s. It never returns {@code null}.
     *
     * @return the mandatory {@link Condition}s. It never returns {@code null}.
     */
    public List<Condition> getMust() {
        return must;
    }

    /**
     * Returns the optional {@link Condition}s. It never returns {@code null}.
     *
     * @return the optional {@link Condition}s. It never returns {@code null}.
     */
    public List<Condition> getShould() {
        return should;
    }

    /**
     * Returns the mandatory not {@link Condition}s. It never returns {@code null}.
     *
     * @return the mandatory not {@link Condition}s. It never returns {@code null}.
     */
    public List<Condition> getNot() {
        return not;
    }

    /**
     * Adds the specified {@link Condition} as mandatory.
     *
     * @param condition the {@link Condition} to be added as mandatory.
     * @return this.
     */
    public BooleanCondition must(Condition condition) {
        must.add(condition);
        return this;
    }

    /**
     * Adds the specified {@link Condition} as optional.
     *
     * @param condition the {@link Condition} to be added as optional.
     * @return this.
     */
    public BooleanCondition should(Condition condition) {
        should.add(condition);
        return this;
    }

    /**
     * Adds the specified {@link Condition} as mandatory not matching.
     *
     * @param condition the {@link Condition} to be added as mandatory not matching.
     * @return this.
     */
    public BooleanCondition not(Condition condition) {
        not.add(condition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query query(Options schema) throws Exception {
        BooleanQuery luceneQuery = new BooleanQuery();
        luceneQuery.setBoost(boost);
        for (Condition query : must) {
            luceneQuery.add(query.query(schema), Occur.MUST);
        }
        for (Condition query : should) {
            luceneQuery.add(query.query(schema), Occur.SHOULD);
        }
        for (Condition query : not) {
            luceneQuery.add(query.query(schema), Occur.MUST_NOT);
        }
        return luceneQuery;
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
        builder.append(", must=");
        builder.append(must);
        builder.append(", should=");
        builder.append(should);
        builder.append(", not=");
        builder.append(not);
        builder.append("]");
        return builder.toString();
    }

}
