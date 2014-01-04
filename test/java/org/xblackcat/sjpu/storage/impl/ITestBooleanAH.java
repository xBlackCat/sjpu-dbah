package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITestBooleanAH extends IAH {
    @Sql("SELECT 1")
    boolean getBoolean() throws StorageException;

    @Sql("SELECT 1")
    Boolean getBooleanObject() throws StorageException;

/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    boolean getBoolean2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Boolean getBooleanObject2() throws StorageException;
*/
}
