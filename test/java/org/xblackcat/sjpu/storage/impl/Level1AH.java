package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 13.12.13 16:32
 *
 * @author xBlackCat
 */
public abstract class Level1AH extends AnAH {
    protected Level1AH(IQueryHelper helper) {
        super(helper);
    }

    @Sql("UPDATE nothing")
    abstract void updateNothing() throws StorageException;

    @Sql("UPDATE nothing")
    protected abstract void updateToOverride() throws StorageException;
}
