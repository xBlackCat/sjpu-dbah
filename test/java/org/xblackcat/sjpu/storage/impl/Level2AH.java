package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

/**
 * 13.12.13 16:33
 *
 * @author xBlackCat
 */
public abstract class Level2AH extends Level1AH {
    protected Level2AH(IConnectionFactory helper) {
        super(helper);
    }

    @Sql("UPDATE nothing")
    public abstract void updateNothingAgain() throws StorageException;

}
