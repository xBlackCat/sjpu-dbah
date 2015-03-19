package org.xblackcat.sjpu.storage.typemap;

import java.sql.Timestamp;
import java.util.Date;

/**
 * A default mapper between java.sql.Timestamp and java.util.Date objects
 * <p>
 * 07.04.2014 16:09
 *
 * @author xBlackCat
 */
@Deprecated
public class DateMapper implements IMapFactory<Date, Timestamp> {
    private static final ITypeMap<Date, Timestamp> TYPE_MAP = new NullPassTypeMap<>(
            Date.class,
            Timestamp.class,
            date -> new Timestamp(date.getTime()),
            timestamp -> new Date(timestamp.getTime())
    );

    @Override
    public boolean isAccepted(Class<?> obj) {
        return Date.class.equals(obj);
    }

    @Override
    public ITypeMap<Date, Timestamp> mapper(Class<Date> clazz) {
        return TYPE_MAP;
    }
}
