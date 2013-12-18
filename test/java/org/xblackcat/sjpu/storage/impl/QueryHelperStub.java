package org.xblackcat.sjpu.storage.impl;


import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 16.08.13 14:07
 *
 * @author xBlackCat
 */
@SuppressWarnings("unchecked")
public class QueryHelperStub implements IQueryHelper {
    private final List data;

    public QueryHelperStub(Object... data) {
        this.data = Arrays.asList(data);
    }

    @Override
    public <T> List<T> execute(
            IToObjectConverter<T> c, String sql, Object... parameters
    ) throws StorageException {
        return new ArrayList<>((Collection<? extends T>) data);
    }

    @Override
    public <T> T executeSingle(
            IToObjectConverter<T> c, String sql, Object... parameters
    ) throws StorageException {
        return data.size() == 0 ? null : (T) data.get(0);
    }

    @Override
    public int update(String sql, Object... parameters) throws StorageException {
        return 1;
    }

    @Override
    public <T> T insert(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        return null;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

}
