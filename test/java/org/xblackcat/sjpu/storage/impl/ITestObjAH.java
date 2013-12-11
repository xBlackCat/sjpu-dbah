package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.RowMap;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 11.12.13 13:41
 *
 * @author xBlackCat
 */
public interface ITestObjAH extends IAH {
    @Sql("SELECT 1")
    SimpleRowData couldBeUsedWithoutAnnotations() throws StorageException;

    @Sql("SELECT 1")
    @RowMap({int.class, String.class})
    RowData mappedByRowMap() throws StorageException;

    @Sql("SELECT 1")
    RowData mappedByDefaultRowMap() throws StorageException;

    @Sql("SELECT 1")
    @RowMap({int.class, String.class})
    RowData mappedByRowMapReuse() throws StorageException;

    @Sql("SELECT 1")
    RowData mappedByDefaultRowMapReuse() throws StorageException;

}
