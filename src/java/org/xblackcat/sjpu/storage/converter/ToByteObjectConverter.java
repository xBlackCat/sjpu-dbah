package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Byte primitive.
 */

public class ToByteObjectConverter implements IToObjectConverter<Byte> {
    public Byte convert(ResultSet rs) throws SQLException {
        final byte v = rs.getByte(1);
        if (rs.wasNull()) {
            return null;
        } else {
            return v;
        }
    }
}