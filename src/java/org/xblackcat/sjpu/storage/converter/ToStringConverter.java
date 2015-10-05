package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Boolean object.
 * <p>
 * Valid values are threaded as <code>true</code>: boolean true, non-zero number value.
 * All other values are threaded as <code>false</code>. NULL threaded as null.
 */

public class ToStringConverter implements IToObjectConverter<String> {
    public String convert(ResultSet rs) throws SQLException {
        return rs.getString(1);
    }
}
