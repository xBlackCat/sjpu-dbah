package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

/**
 * 13.12.13 16:34
 *
 * @author xBlackCat
 */
public abstract class Level3AH extends Level2AH {
    protected Level3AH(IConnectionFactory helper) {
        super(helper);
    }

    @Sql("UPDATE nothing")
    protected abstract void updateNothingMore() throws StorageException;


    @Sql("UPDATE something")
    protected abstract void updateToOverride() throws StorageException;

}
