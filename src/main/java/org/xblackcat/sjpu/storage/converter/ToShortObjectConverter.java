package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Short primitive.
 */

public class ToShortObjectConverter implements IToObjectConverter<Short> {
    public Short convert(ResultSet rs) throws SQLException {
        final short v = rs.getShort(1);
        if (rs.wasNull()) {
            return null;
        } else {
            return v;
        }
    }
}