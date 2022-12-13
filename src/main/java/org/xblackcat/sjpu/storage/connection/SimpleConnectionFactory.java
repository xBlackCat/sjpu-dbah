package org.xblackcat.sjpu.storage.connection;

import org.xblackcat.sjpu.storage.StorageException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleConnectionFactory extends AConnectionFactory {
    private final IDBConfig settings;

    public SimpleConnectionFactory(IDBConfig settings) throws StorageException {
        super(settings.driver());
        this.settings = settings;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                settings.url(),
                settings.user(),
                settings.password()
        );
    }

    @Override
    public void shutdown() {

    }
}
