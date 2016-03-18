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

package com.tuplejump.stargate.lucene.query.function;

import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.cassandra.ResultMapper;
import com.tuplejump.stargate.cassandra.RowFetcher;
import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;

import java.util.List;

/**
 * User: satya
 */
public class NoOp implements Function {

    @Override
    public void init(Options options) {
        //do nothing.
    }

    @Override
    public boolean shouldTryScoring() {
        return true;
    }

    @Override
    public List<Row> process(ResultMapper resultMapper, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        RowFetcher rowFetcher = new RowFetcher(resultMapper);
        return rowFetcher.fetchRows();
    }

    @Override
    public boolean needsPaging() {
        return true;
    }


    public String getFunction() {
        return "no-op";
    }


}

