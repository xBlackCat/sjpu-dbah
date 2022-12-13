package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The class to convert a single-column result to Bytes primitive.
 */

public class ToBytesConverter implements IToObjectConverter<byte[]> {
    public byte[] convert(ResultSet rs) throws SQLException {
        return rs.getBytes(1);
    }
}