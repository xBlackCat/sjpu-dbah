package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITestDoubleAH extends IAH {
    @Sql("SELECT 1")
    double getDouble() throws StorageException;

    @Sql("SELECT 1")
    Double getDoubleObject() throws StorageException;

/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    double getDouble2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Double getDoubleObject2() throws StorageException;
*/
}
