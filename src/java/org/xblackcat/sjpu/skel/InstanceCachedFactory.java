package org.xblackcat.sjpu.skel;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 04.04.2014 15:47
 *
 * @author xBlackCat
 */
public class InstanceCachedFactory<Base> implements IFactory<Base> {
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Map<Key, Base> helpers = new HashMap<>();

    protected final IBuilder<Base> builder;
    private final Class<?>[] argClasses;

    public InstanceCachedFactory(IBuilder<Base> builder, Class<?>... argClasses) {
        this.builder = builder;
        this.argClasses = argClasses;
    }

    public <T extends Base> T get(Class<T> clazz, Object... args) throws GeneratorException {
        Key key = new Key(clazz, args);

        lock.readLock().lock();
        try {
            @SuppressWarnings({"unchecked"})
            T accessHelper = (T) helpers.get(key);

            if (accessHelper != null) {
                return accessHelper;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            final Class<? extends T> builtClass = builder.build(clazz);
            T accessHelper = instantiate(builtClass, args);
            @SuppressWarnings({"unchecked"})
            T oldAccessHelper = (T) helpers.get(key);

            if (oldAccessHelper != null) {
                return oldAccessHelper;
            }

            helpers.put(key, accessHelper);

            return accessHelper;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T extends Base> T instantiate(Class<? extends T> builtClass, Object[] args) throws GeneratorException {
        try {
            return builtClass.getConstructor(argClasses).newInstance(args);
        } catch (InstantiationException e) {
            throw new GeneratorException("Class is not implemented", e);
        } catch (IllegalAccessException e) {
            throw new GeneratorException("Access helper constructor should be public", e);
        } catch (InvocationTargetException e) {
            throw new GeneratorException("Exception occurs in access helper constructor", e);
        } catch (NoSuchMethodException e) {
            throw new GeneratorException(
                    "Access helper class constructor should have the following signature: " +
                            builtClass.getName() + "(" + ArrayUtils.toString(argClasses) + " arg);",
                    e
            );
        }
    }

    @Override
    public ReadWriteLock getLock() {
        return lock;
    }

    @Override
    public void purge() {
        lock.writeLock().lock();
        try {
            helpers.clear();
        } finally {
            lock.writeLock().unlock();
        }

    }

    private final static class Key {
        private final Class<?> clazz;
        private final Object[] args;

        public Key(Class<?> clazz, Object[] args) {
            this.clazz = clazz;
            this.args = args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(clazz, key.clazz) &&
                    Arrays.equals(args, key.args);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, Arrays.hashCode(args));
        }
    }
}
