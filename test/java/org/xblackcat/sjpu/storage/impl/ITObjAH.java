package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.RowMap;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 11.12.13 13:41
 *
 * @author xBlackCat
 */
public interface ITObjAH extends IAH {
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
