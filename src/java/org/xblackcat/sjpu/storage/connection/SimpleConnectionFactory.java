package org.xblackcat.sjpu.storage.connection;

import org.xblackcat.sjpu.storage.StorageException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author ASUS
 */

public class SimpleConnectionFactory extends AConnectionFactory {
    public SimpleConnectionFactory(IDBConfig settings) throws StorageException {
        super(settings);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                settings.getUrl(),
                settings.getUser(),
                settings.getPassword()
        );
    }

    @Override
    public void shutdown() {

    }
}
