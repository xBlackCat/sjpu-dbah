package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Short primitive.
 */

public class ToShortConverter implements IToObjectConverter<Short> {
    public Short convert(ResultSet rs) throws SQLException {
        return rs.getShort(1);
    }
}