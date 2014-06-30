package org.xblackcat.sjpu.skel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 04.04.2014 15:47
 *
 * @author xBlackCat
 */
public class AFactory<Base, Helper> implements IFactory<Base> {
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Map<Class<? extends Base>, Base> helpers = new HashMap<>();

    protected final IBuilder<Base, Helper> builder;
    protected final Helper factory;

    public AFactory(Helper factory, IBuilder<Base, Helper> builder) {
        this.factory = factory;
        this.builder = builder;
    }

    public <T extends Base> T get(Class<T> clazz) throws GeneratorException {
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
            final T accessHelper = builder.build(clazz, factory);
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
