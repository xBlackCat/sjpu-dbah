package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITestShortAH extends IAH {
    @Sql("SELECT 1")
    short getShort() throws StorageException;

    @Sql("SELECT 1")
    Short getShortObject() throws StorageException;

/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    short getShort2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Short getShortObject2() throws StorageException;
*/
}
