package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IStorage;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.IDatabaseSettings;
import org.xblackcat.sjpu.storage.connection.SimplePooledConnectionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 30.01.12 12:47
 *
 * @author xBlackCat
 */
public class Storage implements IStorage {
    private static final InterfaceAHBuilder INTERFACE_AH_BUILDER = new InterfaceAHBuilder();
    private static final AbstractAHBuilder ABSTRACT_AH_BUILDER = new AbstractAHBuilder();
    private static final InstanceAHBuilder INSTANCE_AH_BUILDER = new InstanceAHBuilder();

    private final Map<Class<? extends IAH>, IAH> helpers = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final IQueryHelper queryHelper;

    public Storage(IDatabaseSettings settings) {
        this(buildHelper(settings));
    }

    private static IQueryHelper buildHelper(IDatabaseSettings settings) {
        try {
            return new QueryHelper(new SimplePooledConnectionFactory(settings));
        } catch (StorageException e) {
            throw new RuntimeException("Can not initialize DB connection factory", e);
        }
    }

    public Storage(IQueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    @Override
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

        final IAHBuilder builder = getBuilder(clazz);

        final T accessHelper = builder.build(clazz, queryHelper);

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

    private IAHBuilder getBuilder(Class<?> clazz) {
        if (clazz.isInterface()) {
            return INTERFACE_AH_BUILDER;
        } else {
            if (!AnAH.class.isAssignableFrom(clazz)) {
                throw new StorageSetupException(
                        "Access helper class should have " +
                                AnAH.class.getName() +
                                " as super class"
                );
            }

            if (java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                return ABSTRACT_AH_BUILDER;
            } else {
                return INSTANCE_AH_BUILDER;
            }
        }
    }

    @Override
    public IConnectionFactory getConnectionFactory() {
        return queryHelper.getConnectionFactory();
    }
}
