package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IBatchedAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ABatchedAH implements IBatchedAH {
    protected final Connection con;

    public ABatchedAH(IConnectionFactory factory) throws StorageException {
        try {
            con = factory.getConnection();
        } catch (SQLException e) {
            throw new StorageException("Failed to borrow connection for Batched Access Helper", e);
        }
    }

    @Override
    public void close() throws StorageException {
        try {
            con.close();
        } catch (SQLException e) {
            throw new StorageException("Failed to close connection", e);
        }
    }
}
