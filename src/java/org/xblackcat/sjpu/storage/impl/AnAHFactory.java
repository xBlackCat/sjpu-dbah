package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IAHFactory;
import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 15.11.13 14:22
 *
 * @author xBlackCat
 */
abstract class AnAHFactory implements IAHFactory {
    protected final IAHBuilder<IQueryHelper> ahBuilder;

    protected final Map<Class<? extends IAH>, IAH> helpers = new HashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final IQueryHelper queryHelper;
    protected final TypeMapper typeMapper;
    protected final Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers;
    protected final Definer<AnAH, IQueryHelper> definer;

    AnAHFactory(
            Definer<AnAH, IQueryHelper> definer,
            IQueryHelper queryHelper,
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers
    ) {
        if (rowSetConsumers == null) {
            throw new NullPointerException("RowSet consumer map can't be null");
        }
        this.queryHelper = queryHelper;
        this.typeMapper = typeMapper;
        this.rowSetConsumers = rowSetConsumers;
        this.definer = definer;
        ahBuilder = new AHBuilder<>(typeMapper, this.definer, rowSetConsumers);
    }

    public <T extends IAH> T get(Class<T> clazz) throws StorageSetupException {
        lock.readLock().lock();
        try {
            @SuppressWarnings({"unchecked"})
            T accessHelper = (T) helpers.get(clazz);

            if (accessHelper != null) {
                return accessHelper;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            final T accessHelper = ahBuilder.build(clazz, queryHelper);
            @SuppressWarnings({"unchecked"})
            T oldAccessHelper = (T) helpers.get(clazz);

            if (oldAccessHelper != null) {
                return oldAccessHelper;
            }

            helpers.put(clazz, accessHelper);

            return accessHelper;
        } finally {
            lock.writeLock().unlock();
        }
    }

}
