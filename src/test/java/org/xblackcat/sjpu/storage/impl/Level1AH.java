package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

/**
 * 13.12.13 16:32
 *
 * @author xBlackCat
 */
public abstract class Level1AH extends AnAH implements ILevel1AH {
    protected Level1AH(IConnectionFactory helper) {
        super(helper);
    }

    @Sql("UPDATE nothing")
    abstract void updateNothing() throws StorageException;

    @Sql("UPDATE nothing")
    protected abstract void updateToOverride() throws StorageException;
}
