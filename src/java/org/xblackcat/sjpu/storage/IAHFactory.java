package org.xblackcat.sjpu.storage;

/**
 * 15.11.13 14:17
 *
 * @author xBlackCat
 */
public interface IAHFactory {
    <T extends IAH> T get(Class<T> clazz);
}
