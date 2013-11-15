package org.xblackcat.sjpu.storage;

/**
 * 12.02.13 15:47
 *
 * @author xBlackCat
 */
public interface IStorage {
    <T extends IAH> T get(Class<T> clazz);

}
