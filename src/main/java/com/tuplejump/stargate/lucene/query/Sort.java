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
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Iterator;
import java.util.List;

/**
 * A sort order of fields
 */
public class Sort implements Iterable<SortField> {

    /**
     * Per field sort.
     */
    private final List<SortField> sortFields;

    @JsonCreator
    public Sort(@JsonProperty("fields") List<SortField> sortFields) {
        this.sortFields = sortFields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<SortField> iterator() {
        return sortFields.iterator();
    }

    /**
     * Returns the {@link SortField}s to be used.
     *
     * @return
     */
    public List<SortField> getSortFields() {
        return sortFields;
    }

    /**
     * Returns the Lucene's {@link org.apache.lucene.search.Sort} representing this {@link Sort}.
     *
     * @param schema
     * @return the Lucene's {@link org.apache.lucene.search.SortField[]} representing this {@link Sort}.
     */
    public org.apache.lucene.search.SortField[] sort(Options schema) {
        org.apache.lucene.search.SortField[] sortFields = new org.apache.lucene.search.SortField[this.sortFields.size()];
        for (int i = 0; i < this.sortFields.size(); i++) {
            sortFields[i] = this.sortFields.get(i).sortField(schema);
        }
        return sortFields;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sort [sortFields=");
        builder.append(sortFields);
        builder.append("]");
        return builder.toString();
    }

}
