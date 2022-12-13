package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VoidConverter implements IToObjectConverter<Void> {
    @Override
    public Void convert(ResultSet rs) throws SQLException {
        return null;
    }
}
