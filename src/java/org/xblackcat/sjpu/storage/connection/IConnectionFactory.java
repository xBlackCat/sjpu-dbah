package org.xblackcat.sjpu.storage.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ASUS
 */

public interface IConnectionFactory {
    Connection getWriteConnection() throws SQLException;

    Connection getReadConnection() throws SQLException;

}
