package org.xblackcat.sjpu.storage.connection;

import org.xblackcat.sjpu.storage.StorageException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author ASUS
 */

public class SimpleConnectionFactory extends AConnectionFactory {
    public SimpleConnectionFactory(IDatabaseSettings settings) throws StorageException {
        super(settings);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                settings.getDbConnectionUrlPattern(),
                settings.getDbAccessUser(),
                settings.getDbAccessPassword()
        );
    }

    @Override
    public void shutdown() {

    }
}
