package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

/**
 * 12.02.13 15:47
 *
 * @author xBlackCat
 */
public interface IStorage {
    <T extends IAH> T get(Class<T> clazz);

    IConnectionFactory getConnectionFactory();
}
