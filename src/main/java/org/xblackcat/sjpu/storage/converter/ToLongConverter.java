package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Long primitive.
 */

public class ToLongConverter implements IToObjectConverter<Long> {
    public Long convert(ResultSet rs) throws SQLException {
        return rs.getLong(1);
    }
}