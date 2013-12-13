package org.xblackcat.sjpu.storage.converter;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 13.12.13 17:28
 *
 * @author xBlackCat
 */
public class ToObjectUtils {
    public static Date toDate(Timestamp ts) {
        if (ts == null) {
            return null;
        }

        return new Date(ts.getTime());
    }
}
