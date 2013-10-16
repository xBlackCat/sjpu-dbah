package org.xblackcat.sjpu.storage.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ASUS
 */

public interface IToObjectConverter<T> {
    /**
     * Converts a current row in ResultSet object to correspond object.
     *
     * @param rs result of query.
     * @return a new object from ResultSet row fields
     * @throws java.sql.SQLException if any database related storage is affected.
     */
    T convert(ResultSet rs) throws SQLException;
}
