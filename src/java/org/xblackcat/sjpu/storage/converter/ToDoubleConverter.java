package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Double primitive.
 */

public class ToDoubleConverter implements IToObjectConverter<Double> {
    public Double convert(ResultSet rs) throws SQLException {
        return rs.getDouble(1);
    }
}