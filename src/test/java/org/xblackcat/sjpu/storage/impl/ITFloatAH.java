package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITFloatAH extends IAH {
    @Sql("SELECT 1")
    float getFloat() throws StorageException;

    @Sql("SELECT 1")
    Float getFloatObject() throws StorageException;

/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    float getFloat2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Float getFloatObject2() throws StorageException;
*/
}
