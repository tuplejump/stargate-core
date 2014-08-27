/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuplejump.stargate.lucene.json;

import com.google.common.base.Joiner;
import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.lucene.Properties;
import org.apache.lucene.document.FieldType;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.util.ArrayList;
import java.util.List;

/**
 * User: satya
 * A JsonDocument which uses a streaming json parser to construct the list of fields.
 * This uses lesser memory than the regular json document which has a DOM based parser.
 */
public class StreamingJsonDocument extends JsonDocument {


    /**
     * @param json        The json to parse and make fields.
     * @param properties  The mapping for making lucene fields from the Json.
     * @param jsonColName
     */
    public StreamingJsonDocument(String json, Properties properties, String jsonColName) {
        super(properties, jsonColName);
        try {
            JsonParser jp = jsonFactory.createJsonParser(json);
            List<String> fieldName = new ArrayList<>();
            String levelFieldName = null;
            JsonToken current = jp.nextToken();
            JsonToken last = null;
            while (current != null) {
                Properties currentProps = getProps(fieldName);
                FieldType fieldType = currentProps != null ? currentProps.dynamicFieldType() : null;
                String currentFieldName = Joiner.on('.').join(fieldName);
                switch (current) {
                    case START_OBJECT:
                        pushLevelFieldName(fieldName, levelFieldName);
                        break;

                    case END_OBJECT:
                        if (fieldName.size() > 0) {
                            levelFieldName = fieldName.remove(fieldName.size() - 1);
                        }
                        break;

                    case START_ARRAY:
                        pushLevelFieldName(fieldName, levelFieldName);
                        levelFieldName = null;

                    case END_ARRAY:
                        if (fieldName.size() > 0)
                            fieldName.remove(fieldName.size() - 1);
                        if (last == JsonToken.END_OBJECT)
                            levelFieldName = null;
                        break;

                    case FIELD_NAME:
                        levelFieldName = jp.getText();
                        fieldName.add(levelFieldName);
                        break;

                    case VALUE_STRING:
                        if (currentProps == null) {
                            fields.add(Fields.textField(currentFieldName, jp.getText()));
                        } else {
                            fields.add(Fields.field(currentFieldName, currentProps, jp.getText(), fieldType));
                        }
                        popLevelFieldName(fieldName, levelFieldName);
                        break;

                    case VALUE_NUMBER_FLOAT:
                        if (currentProps == null) {
                            fields.add(Fields.doubleField(currentFieldName, jp.getText()));
                        } else {
                            fields.add(Fields.field(currentFieldName, currentProps, jp.getText(), fieldType));
                        }
                        popLevelFieldName(fieldName, levelFieldName);
                        break;

                    case VALUE_NUMBER_INT:
                        if (currentProps == null) {
                            fields.add(Fields.longField(currentFieldName, jp.getText()));
                        } else {
                            fields.add(Fields.field(currentFieldName, currentProps, jp.getText(), fieldType));
                        }
                        popLevelFieldName(fieldName, levelFieldName);
                        break;

                    case VALUE_TRUE:
                        fields.add(Fields.stringField(currentFieldName, "true"));
                        popLevelFieldName(fieldName, levelFieldName);
                        break;

                    case VALUE_FALSE:
                        fields.add(Fields.stringField(currentFieldName, "false"));
                        popLevelFieldName(fieldName, levelFieldName);
                        break;

                    case VALUE_NULL:
                        fields.add(Fields.stringField(currentFieldName, "_NULL_"));
                        popLevelFieldName(fieldName, levelFieldName);
                        break;

                    default:
                        //do nothing
                        break;
                }
                last = current;
                current = jp.nextToken();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void pushLevelFieldName(List<String> fieldName, String levelFieldName) {
        if (levelFieldName != null) {
            fieldName.add(levelFieldName);
        }
    }

    private void popLevelFieldName(List<String> fieldName, String levelFieldName) {
        fieldName.remove(levelFieldName);
    }
}
