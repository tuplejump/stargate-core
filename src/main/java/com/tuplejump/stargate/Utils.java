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

import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;

/**
 * User: satya
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);


    //  NumberFormat instances are not thread safe
    public static final ThreadLocal<NumberFormat> numberFormatThreadLocal =
            new ThreadLocal<NumberFormat>() {
                @Override
                public NumberFormat initialValue() {
                    NumberFormat fmt = NumberFormat.getInstance();
                    fmt.setGroupingUsed(false);
                    fmt.setMinimumIntegerDigits(4);
                    return fmt;
                }
            };

    public static class NumericConfigTL extends NumericConfig {

        static NumberFormat dummyInstance = NumberFormat.getInstance();

        public NumericConfigTL(int precisionStep, FieldType.NumericType type) {
            super(precisionStep, dummyInstance, type);
        }

        @Override
        public NumberFormat getNumberFormat() {
            return numberFormatThreadLocal.get();
        }
    }

    public static NumericConfig numericConfig(FieldType fieldType) {
        if (fieldType.numericType() != null) {
            NumericConfig numConfig = new NumericConfigTL(fieldType.numericPrecisionStep(), fieldType.numericType());
            return numConfig;
        }
        return null;
    }


    public static File getDirectory(String ksName, String cfName, String indexName, String vNodeName) throws IOException {
        String fileName = indexName;
        String dirName = Options.defaultIndexesDir;
        dirName = dirName + File.separator + ksName + File.separator + cfName + File.separator + vNodeName;
        logger.debug("SGIndex - INDEX_FILE_NAME -" + fileName);
        logger.debug("SGIndex - INDEX_DIR_NAME -" + dirName);
        //will only create parent if not existing.
        return new File(dirName, fileName);
    }

    public static String getColumnNameStr(ByteBuffer colName) {
        String s = CFDefinition.definitionType.getString(colName);
        s = StringUtils.removeStart(s, ".").trim();
        return s;
    }

    public static String getColumnName(ColumnDefinition cd) {
        return CFDefinition.definitionType.getString(cd.name);
    }

    public static SimpleTimer getStartedTimer(Logger logger) {
        SimpleTimer timer = new SimpleTimer(logger);
        timer.start();
        return timer;
    }

    public static SimpleTimer getStartedTimer() {
        SimpleTimer timer = new SimpleTimer();
        timer.start();
        return timer;
    }

    public static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //do nothing
        }
    }

    public static class SimpleTimer {
        long startTime;
        long endTime;
        Logger logger;

        SimpleTimer(Logger logger) {
            this.logger = logger;
        }

        SimpleTimer() {
        }

        public void start() {
            startTime = System.nanoTime();
        }

        public void end() {
            endTime = System.nanoTime();
        }

        public double time() {
            return timeNano() / 1000000;
        }

        public long timeNano() {
            return endTime - startTime;
        }

        public void logTime(String prefix) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("{} - time taken is [{}] milli seconds"), prefix, time());
        }

        public void endLogTime(String prefix) {
            end();
            logTime(prefix);
        }

        public double endGetTime() {
            end();
            return time();
        }

        public long endGetTimeNano() {
            end();
            return timeNano();
        }

    }

}
