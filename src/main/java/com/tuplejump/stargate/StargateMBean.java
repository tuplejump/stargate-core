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

package com.tuplejump.stargate;

import java.io.IOException;

/**
 * User: satya
 */
public interface StargateMBean {

    public static final String MBEAN_NAME = "com.tuplejump.stargate:type=Super";

    public String[] allIndexes();

    public String[] indexShards(String indexName);

    public String describeIndex(String indexName) throws IOException;

    public long indexSize(String indexName);

    public long indexLiveSize(String indexName);

    public long writeGeneration();

    public long readGeneration();


}
