package org.xblackcat.sjpu.storage.converter;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to BigDecimal primitive.
 */

public class ToBigDecimalConverter implements IToObjectConverter<BigDecimal> {
    public BigDecimal convert(ResultSet rs) throws SQLException {
        return rs.getBigDecimal(1);
    }
}