package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITestByteAH extends IAH {
    @Sql("SELECT 1")
    byte getByte() throws StorageException;

    @Sql("SELECT 1")
    Byte getByteObject() throws StorageException;

/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    byte getByte2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Byte getByteObject2() throws StorageException;
*/
}
