package org.xblackcat.sjpu.storage.connection;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.xblackcat.sjpu.storage.StorageException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xBlackCat
 */

public class SimplePooledConnectionFactory extends AConnectionFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger();

    private final String poolName;

    public SimplePooledConnectionFactory(IDBConfig settings) throws StorageException {
        super(settings);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                this.settings.getUrl(),
                this.settings.getUser(),
                this.settings.getPassword()
        );

        final int poolSize = settings.getPoolSize();

        final PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setValidationQuery("SELECT 1+1");

        final GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);

        poolableConnectionFactory.setPool(connectionPool);

        connectionPool.setMaxIdle(poolSize);
        connectionPool.setMinIdle(poolSize);
        connectionPool.setMaxTotal(poolSize);
        connectionPool.setTestOnBorrow(true);
        connectionPool.setTestWhileIdle(true);
        connectionPool.setTimeBetweenEvictionRunsMillis(5000);
        connectionPool.setBlockWhenExhausted(true);

        try {
            Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        } catch (ClassNotFoundException e) {
            throw new StorageException("Can not initialize pooling driver", e);
        }

        try {
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

            poolName = "storage" + POOL_NUMBER.incrementAndGet();
            driver.registerPool(poolName, connectionPool);
        } catch (SQLException e) {
            throw new StorageException("Can not obtain pooling driver", e);
        } catch (Exception e) {
            throw new StorageException("Failed to pre-initialize pool", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:apache:commons:dbcp:" + poolName);
    }

    @Override
    public void shutdown() throws StorageException {
        try {
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
            driver.closePool(poolName);
        } catch (Exception e) {
            throw new StorageException("Can't dismiss connection pool", e);
        }
    }
}
