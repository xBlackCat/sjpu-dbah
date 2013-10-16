package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.util.List;

/**
 * @author ASUS
 */

public interface IQueryHelper {
    <T> List<T> execute(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    <T> T executeSingle(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    int update(String sql, Object... parameters) throws StorageException;

    IConnectionFactory getConnectionFactory();
}
