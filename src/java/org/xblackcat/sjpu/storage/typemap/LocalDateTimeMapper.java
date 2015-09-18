package org.xblackcat.sjpu.storage.typemap;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * A default mapper between java.sql.Timestamp and java.util.Date objects
 * <p>
 * 07.04.2014 16:09
 *
 * @author xBlackCat
 */
public class LocalDateTimeMapper implements IMapFactory<LocalDateTime, Timestamp> {
    private final ITypeMap<LocalDateTime, Timestamp> typeMap =
            new NullPassTypeMap<>(
                    LocalDateTime.class,
                    Timestamp.class,
                    (Function<LocalDateTime, Timestamp>) Timestamp::valueOf,
                    Timestamp::toLocalDateTime
            );

    @Override
    public boolean isAccepted(Class<?> obj) {
        return LocalDateTime.class.equals(obj);
    }

    @Override
    public ITypeMap<LocalDateTime, Timestamp> mapper(Class<LocalDateTime> clazz) {
        return typeMap;
    }

}
