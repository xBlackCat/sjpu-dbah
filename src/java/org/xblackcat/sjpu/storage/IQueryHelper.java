package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 21.11.13 17:50
 *
 * @author xBlackCat
 */
public interface IQueryHelper {
    <T> void execute(IRowConsumer<T> consumer, IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    int update(String sql, Object... parameters) throws StorageException;

    <T> T insert(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    Connection getConnection() throws SQLException;

    void shutdown() throws StorageException;
}
