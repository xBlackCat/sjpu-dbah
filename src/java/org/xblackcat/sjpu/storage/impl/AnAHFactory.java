package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IAHFactory;
import org.xblackcat.sjpu.storage.StorageSetupException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 15.11.13 14:22
 *
 * @author xBlackCat
 */
public abstract class AnAHFactory implements IAHFactory {
    protected static final AHBuilder<AnAH, AQueryHelper> ahBuilder = new AHBuilder<>(new Definer<>(AnAH.class, AQueryHelper.class));
    protected final Map<Class<? extends IAH>, IAH> helpers = new HashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final AQueryHelper queryHelper;

    public AnAHFactory(AQueryHelper queryHelper) {
        this.queryHelper = queryHelper;
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

        final T accessHelper = ahBuilder.build(clazz, queryHelper);

        lock.writeLock().lock();
        try {
            @SuppressWarnings({"unchecked"})
            T oldAccessHelper = (T) helpers.get(clazz);

            if (oldAccessHelper != null) {
                return oldAccessHelper;
            }

            helpers.put(clazz, accessHelper);
        } finally {
            lock.writeLock().unlock();
        }

        return accessHelper;
    }

}
