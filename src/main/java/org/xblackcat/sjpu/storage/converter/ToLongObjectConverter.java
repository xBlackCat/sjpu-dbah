package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Long primitive.
 */

public class ToLongObjectConverter implements IToObjectConverter<Long> {
    public Long convert(ResultSet rs) throws SQLException {
        final long v = rs.getLong(1);
        if (rs.wasNull()) {
            return null;
        } else {
            return v;
        }
    }
}