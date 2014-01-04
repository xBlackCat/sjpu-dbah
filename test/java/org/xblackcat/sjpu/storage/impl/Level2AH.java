package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 13.12.13 16:33
 *
 * @author xBlackCat
 */
public abstract class Level2AH extends Level1AH {
    protected Level2AH(IQueryHelper helper) {
        super(helper);
    }

    @Sql("UPDATE nothing")
    protected abstract void updateNothingAgain() throws StorageException;

}
