package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result as specified object.
 */

public class ToScalarConverter<T> implements IToObjectConverter<T> {
    @Override
    @SuppressWarnings({"unchecked"})
    public T convert(ResultSet rs) throws SQLException {
        return (T) rs.getObject(1);
    }
}
