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
public interface ITestObjFailAH extends IAH {
    @Sql("SELECT 1")
    @RowMap({int.class, int.class})
    FailData getException() throws StorageException;

}
