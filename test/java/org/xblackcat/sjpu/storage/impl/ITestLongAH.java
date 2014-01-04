package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITestLongAH extends IAH {
    @Sql("SELECT 1")
    long getLong() throws StorageException;

    @Sql("SELECT 1")
    Long getLongObject() throws StorageException;

/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    long getLong2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Long getLongObject2() throws StorageException;
*/
}
