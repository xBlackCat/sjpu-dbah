package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.DDL;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 11.12.13 13:41
 *
 * @author xBlackCat
 */
public interface ITObjFail3AH extends IAH {
    @Sql("SELECT 1")
    @DDL("SET VAR=1")
    NoDefaultData getException() throws StorageException;
}
