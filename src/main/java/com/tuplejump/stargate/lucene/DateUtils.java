package com.tuplejump.stargate.lucene;

import java.util.Date;

public class DateUtils {
    public static Long getTimeByGranularity(Date dt, Long granularity) {
        Long millis = dt.getTime();
        return millis - (millis % granularity);
    }
}
