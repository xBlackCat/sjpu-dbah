package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * The class to convert a single-column result to Timestamp primitive.
 */

public class ToDateConverter implements IToObjectConverter<Date> {
    public Date convert(ResultSet rs) throws SQLException {
        return new Date(rs.getTimestamp(1).getTime());
    }
}