package org.xblackcat.sjpu.storage.typemap;

import org.xblackcat.sjpu.util.function.FunctionEx;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class InstantMapper implements IMapFactory<Instant, Timestamp> {
    private final ITypeMap<Instant, Timestamp> typeMap =
            new NullPassTypeMap<>(
                    Instant.class,
                    Timestamp.class,
                    (FunctionEx<Instant, Timestamp, SQLException>) Timestamp::from,
                    Timestamp::toInstant
            );

    @Override
    public boolean isAccepted(Class<?> obj) {
        return Instant.class.equals(obj);
    }

    @Override
    public ITypeMap<Instant, Timestamp> mapper(Class<Instant> clazz) {
        return typeMap;
    }

}