package org.xblackcat.sjpu.storage;

/**
 * 07.09.11 11:07
 *
 * @author xBlackCat
 */
public interface ISimpleAH<V> extends IAH {
    V get(int id) throws StorageException;
}
