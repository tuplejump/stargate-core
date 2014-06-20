package com.tuplejump.stargate;

import com.tuplejump.stargate.lucene.Options;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.tuplejump.stargate.Constants.*;

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

    public static FieldType fieldType(Map<String, String> options, String cfName, String name, AbstractType validator) {
        return Fields.fieldType(options, cfName, name, validator);
    }

    public static NumericConfig numericConfig(Map<String, String> options, FieldType fieldType) {
        if (fieldType.numericType() != null) {
            NumericConfig numConfig = new NumericConfigTL(fieldType.numericPrecisionStep(), fieldType.numericType());
            return numConfig;
        }
        return null;
    }

    public static List<Field> fields(ColumnDefinition columnDef, String colName, ByteBuffer value, FieldType... fieldTypes) {
        List<Field> fields = new ArrayList<>(fieldTypes.length);
        for (FieldType fieldType : fieldTypes) {
            if (logger.isTraceEnabled())
                logger.trace("Col name is " + colName);
            fields.add(Fields.field(colName, columnDef.getValidator(), value, fieldType));
        }
        return fields;
    }

    public static List<Field> idFields(String cfName, ByteBuffer pk, AbstractType rkValValidator) {
        return Arrays.asList(Fields.idDocValues(rkValValidator, pk));
    }



    public static ByteBuffer[] getCompositePKComponents(ColumnFamilyStore baseCfs, ByteBuffer pk) {
        CompositeType baseComparator = (CompositeType) baseCfs.getComparator();
        return baseComparator.split(pk);
    }

    public static ByteBuffer getRowKeyFromPKComponents(ByteBuffer[] pkComponents) {
        return pkComponents[0];
    }


    public static List<Field> tsFields(long ts, String cfName) {
        FieldType tsFieldType = Fields.fieldType(Options.idFieldOptions(), cfName, CF_TS_INDEXED, CQL3Type.Native.BIGINT.getType());
        Field tsField = Fields.tsField(ts, tsFieldType);
        return Arrays.asList(Fields.tsDocValues(ts), tsField);
    }

    public static File getDirectory(String ksName, String cfName, Map<String, String> options) throws IOException {
        String fileName = options.get(INDEX_FILE_NAME);
        String dirName = options.get(INDEX_DIR_NAME);
        dirName = dirName + File.separator + ksName + File.separator + cfName;
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
