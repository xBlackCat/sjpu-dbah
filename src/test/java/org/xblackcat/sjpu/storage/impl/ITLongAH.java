package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;

/**
 * 16.08.13 14:53
 *
 * @author xBlackCat
 */
public interface ITLongAH extends IAH {
    @Sql("SELECT 1")
    long getLong() throws StorageException;

    @Sql("SELECT 1")
    Long getLongObject() throws StorageException;

    @Sql("SELECT 1")
    @MapRowTo(Long.class)
    void getLongObject(IRowConsumer<Long> consumer) throws StorageException;


/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    long getLong2() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    Long getLongObject2() throws StorageException;
*/
}
