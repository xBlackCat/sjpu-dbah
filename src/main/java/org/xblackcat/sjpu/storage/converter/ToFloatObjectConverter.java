package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Float primitive.
 */

public class ToFloatObjectConverter implements IToObjectConverter<Float> {
    public Float convert(ResultSet rs) throws SQLException {
        final float v = rs.getFloat(1);
        if (rs.wasNull()) {
            return null;
        } else {
            return v;
        }
    }
}