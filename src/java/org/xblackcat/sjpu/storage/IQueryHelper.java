package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 21.11.13 17:50
 *
 * @author xBlackCat
 */
public interface IQueryHelper {
    <T> List<T> execute(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    <T> void execute(IRowConsumer<T> consumer, IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    <T> T executeSingle(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    int update(String sql, Object... parameters) throws StorageException;

    <T> T insert(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    Connection getConnection() throws SQLException;

    void shutdown() throws StorageException;
}
