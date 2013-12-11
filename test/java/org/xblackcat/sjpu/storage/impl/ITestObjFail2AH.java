package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 11.12.13 13:41
 *
 * @author xBlackCat
 */
public interface ITestObjFail2AH extends IAH {
    @Sql("SELECT 1")
    NoDefaultData getException() throws StorageException;
}
