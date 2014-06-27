package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Byte primitive.
 */

public class ToByteConverter implements IToObjectConverter<Byte> {
    public Byte convert(ResultSet rs) throws SQLException {
        return rs.getByte(1);
    }
}