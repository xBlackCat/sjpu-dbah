package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Float primitive.
 */

public class ToFloatConverter implements IToObjectConverter<Float> {
    public Float convert(ResultSet rs) throws SQLException {
        return rs.getFloat(1);
    }
}