package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITestIntAH extends IAH {
    @Sql("SELECT 1")
    int getInt() throws StorageException;

    @Sql("SELECT 1")
    Integer getInteger() throws StorageException;
/*

    @GetObject(value = "none", fields = {@QueryField("none")})
    int getInt2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Integer getInteger2() throws StorageException;
*/
}
