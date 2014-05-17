package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Double primitive.
 */

public class ToDoubleObjectConverter implements IToObjectConverter<Double> {
    public Double convert(ResultSet rs) throws SQLException {
        final double v = rs.getDouble(1);
        if (rs.wasNull()) {
            return null;
        } else {
            return v;
        }
    }
}