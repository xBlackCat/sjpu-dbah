package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.util.List;

/**
 * 15.02.14 11:35
 *
 * @author xBlackCat
 */
public abstract class AQueryHelper implements IQueryHelper {
    @Override
    public <T> List<T> execute(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        ToListConsumer<T> consumer = new ToListConsumer<>();

        execute(consumer, c, sql, parameters);

        return consumer.getList();
    }

    @Override
    public <T> T executeSingle(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        SingletonConsumer<T> consumer = new SingletonConsumer<>();

        execute(consumer, c, sql, parameters);

        return consumer.getObject();
    }
}
