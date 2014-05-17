package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 21.02.13 11:05
 *
 * @author xBlackCat
 */
public class VoidConverter implements IToObjectConverter<Void> {
    @Override
    public Void convert(ResultSet rs) throws SQLException {
        return null;
    }
}
