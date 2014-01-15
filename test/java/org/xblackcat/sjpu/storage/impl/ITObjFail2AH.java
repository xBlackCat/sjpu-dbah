package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 11.12.13 13:41
 *
 * @author xBlackCat
 */
public interface ITObjFail2AH extends IAH {
    @Sql("SELECT 1")
    NoDefaultData getException() throws StorageException;
}
