package org.xblackcat.sjpu.storage.connection;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.xblackcat.sjpu.storage.StorageException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author xBlackCat
 */

public class SimplePooledConnectionFactory extends AConnectionFactory {
    public SimplePooledConnectionFactory(IDatabaseSettings settings) throws StorageException {
        super(settings);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                this.settings.getDbConnectionUrlPattern(),
                this.settings.getDbAccessUser(),
                this.settings.getDbAccessPassword()
        );

        ObjectPool connectionPool = new GenericObjectPool<Object>(
                null,
                this.settings.getDbPoolSize(),
                GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
                0,
                13,
                13,
                true,
                true,
                5000,
                5,
                1000,
                true
        );
        new PoolableConnectionFactory(
                connectionFactory,
                connectionPool,
                null,
                "SELECT 1+1",
                false,
                true
        );

        try {
            Class.forName("org.apache.commons.dbcp.PoolingDriver");
        } catch (ClassNotFoundException e) {
            throw new StorageException("Can not initialize pooling driver", e);
        }

        try {
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

            driver.registerPool("storage", connectionPool);
        } catch (SQLException e) {
            throw new StorageException("Can not obtain pooling driver", e);
        }
    }

    public Connection getWriteConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:apache:commons:dbcp:storage");
    }

    @Override
    public Connection getReadConnection() throws SQLException {
        return getWriteConnection();
    }

}
