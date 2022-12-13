package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 16.08.13 15:55
 *
 * @author xBlackCat
 */
public interface IPrimitiveAH extends IAH {
    @Sql("SELECT NULL")
    int getIntBySqlAnn() throws StorageException;

    @Sql("SELECT NULL")
    double getDoubleBySqlAnn() throws StorageException;

    @Sql("SELECT NULL")
    float getFloatBySqlAnn() throws StorageException;

    @Sql("SELECT NULL")
    short getShortBySqlAnn() throws StorageException;

    @Sql("SELECT NULL")
    boolean getBoolBySqlAnn() throws StorageException;

    @Sql("SELECT NULL")
    long getLongBySqlAnn() throws StorageException;

    @Sql("SELECT NULL")
    byte getByteBySqlAnn() throws StorageException;

/*
    @GetObject(value = "none", fields = {@QueryField("none")})
    int getInt2ByGetObjectAnn() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    double getDouble2ByGetObjectAnn() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    float getFloat2ByGetObjectAnn() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    short getShort2ByGetObjectAnn() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    boolean getBool2ByGetObjectAnn() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    long getLong2ByGetObjectAnn() throws StorageException;

    @GetObject(value = "none", fields = {@QueryField("none")})
    byte getByte2ByGetObjectAnn() throws StorageException;
*/
}
