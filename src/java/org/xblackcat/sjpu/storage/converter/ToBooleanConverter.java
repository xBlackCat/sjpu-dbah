package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Boolean object.
 * <p/>
 * Valid values are threaded as <code>true</code>: boolean true, non-zero number value.
 * All other values are threaded as <code>false</code>. NULL threaded as null.
 */

public class ToBooleanConverter implements IToObjectConverter<Boolean> {
    public Boolean convert(ResultSet rs) throws SQLException {
        Object value = rs.getObject(1);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return value instanceof Number && ((Number) value).intValue() != 0;
    }
}
