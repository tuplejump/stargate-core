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

package com.tuplejump.stargate.lucene;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.lucene.search.IndexSearcher;

/**
 * Interface for a searcher callback.
 * The callback method will be called with the searcher. This callback is in place so that lucene IndexSearcher resources
 * are cleaned up properly
 * User: satya
 */
public interface SearcherCallback<T> {

    public T doWithSearcher(IndexSearcher searcher) throws Exception;

    public Range<Token> filterRange();

    public boolean isSingleToken();

    public boolean isFullRange();
}
