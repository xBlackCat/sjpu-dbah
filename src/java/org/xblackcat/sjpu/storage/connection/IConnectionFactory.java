package org.xblackcat.sjpu.storage.connection;

import org.xblackcat.sjpu.storage.StorageException;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnectionFactory {
    Connection getConnection() throws SQLException;

    void shutdown() throws StorageException;
}
