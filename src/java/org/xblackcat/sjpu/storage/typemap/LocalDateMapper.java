package org.xblackcat.sjpu.storage.typemap;

import org.xblackcat.sjpu.util.function.FunctionEx;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * A default mapper between java.sql.Timestamp and java.util.Date objects
 * <p>
 * 07.04.2014 16:09
 *
 * @author xBlackCat
 */
public class LocalDateMapper implements IMapFactory<LocalDate, Date> {
    private final ITypeMap<LocalDate, Date> typeMap = new NullPassTypeMap<>(
            LocalDate.class,
            Date.class,
            (FunctionEx<LocalDate, Date, SQLException>) Date::valueOf,
            Date::toLocalDate
    );

    @Override
    public boolean isAccepted(Class<?> obj) {
        return LocalDate.class.equals(obj);
    }

    @Override
    public ITypeMap<LocalDate, Date> mapper(Class<LocalDate> clazz) {
        return typeMap;
    }

}
