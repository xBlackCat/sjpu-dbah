package org.xblackcat.sjpu.storage.impl;


import org.xblackcat.sjpu.storage.ConsumeException;
import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.Connection;
import java.util.Arrays;
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
    public <T> void execute(
            IRowConsumer<T> consumer, IToObjectConverter<T> c, String sql, Object... parameters
    ) throws StorageException {
        try {
            for (Object row : data) {
                T obj = (T) row;
                consumer.consume(obj);
            }
        } catch (ConsumeException | RuntimeException e) {
            throw new StorageException("Can not consume result for query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e);
        }
    }

    @Override
    public int update(String sql, Object... parameters) throws StorageException {
        return 1;
    }

    @Override
    public int update(String sql) throws StorageException {
        return 0;
    }

    @Override
    public <T> T insert(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        return null;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void shutdown() throws StorageException {

    }

}
