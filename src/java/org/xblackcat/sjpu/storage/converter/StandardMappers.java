package org.xblackcat.sjpu.storage.converter;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 18.12.13 16:08
 *
 * @author xBlackCat
 */
public class StandardMappers {
    public static Date timestampToDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return new Date(timestamp.getTime());
    }

    public static Timestamp dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        }

        final Timestamp timestamp = new Timestamp(date.getTime());
        timestamp.setNanos(0);
        return timestamp;
    }
}
