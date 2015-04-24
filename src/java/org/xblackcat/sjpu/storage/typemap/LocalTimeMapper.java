package org.xblackcat.sjpu.storage.typemap;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A default mapper between java.sql.Timestamp and java.util.Date objects
 * <p>
 * 07.04.2014 16:09
 *
 * @author xBlackCat
 */
public class LocalTimeMapper implements IMapFactory<LocalTime, Time> {
    private final ITypeMap<LocalTime, Time> typeMap = new NullPassTypeMap<>(LocalTime.class, Time.class, Time::valueOf, Time::toLocalTime);

    @Override
    public boolean isAccepted(Class<?> obj) {
        return LocalDateTime.class.equals(obj);
    }

    @Override
    public ITypeMap<LocalTime, Time> mapper(Class<LocalTime> clazz) {
        return typeMap;
    }

}
