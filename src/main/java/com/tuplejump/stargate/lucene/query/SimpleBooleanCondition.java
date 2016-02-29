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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * User: satya
 */
public class SimpleBooleanCondition extends Condition {

    private List<Condition> conditions;

    private GroupType groupType;

    /**
     * Returns a new {@link BooleanCondition} compound by the specified {@link Condition}s.
     *
     * @param boost      The boost for this query clause. Documents matching this clause will (in addition to the normal
     *                   weightings) have their score multiplied by {@code boost}.
     * @param type       the optional {@link com.tuplejump.stargate.lucene.query.GroupType}
     * @param conditions the mandatory {@link Condition}s.
     */
    @JsonCreator
    public SimpleBooleanCondition(@JsonProperty("boost") Float boost, @JsonProperty("type") GroupType type,
                                  @JsonProperty("conditions") List<Condition> conditions) {
        super(boost);
        this.conditions = conditions == null ? new LinkedList<Condition>() : conditions;
        this.groupType = type == null ? GroupType.AND : type;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Query query(Options schema) throws Exception {
        BooleanQuery.Builder luceneQuery = new BooleanQuery.Builder();
        if (groupType == GroupType.OR) {
            for (Condition query : conditions) {
                luceneQuery.add(query.query(schema), BooleanClause.Occur.SHOULD);
            }
        } else if (groupType == GroupType.AND) {
            for (Condition query : conditions) {
                luceneQuery.add(query.query(schema), BooleanClause.Occur.MUST);
            }
        } else {
            for (Condition query : conditions) {
                luceneQuery.add(query.query(schema), BooleanClause.Occur.MUST_NOT);
            }

        }
        return luceneQuery.build();
    }

    @Override
    public String getType() {
        return "bool";
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
        builder.append(", conditions=");
        builder.append(conditions);
        builder.append(", type=");
        builder.append(groupType);
        builder.append("]");
        return builder.toString();
    }

}
