package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 13.12.13 16:33
 *
 * @author xBlackCat
 */
public abstract class Level2AH extends Level1AH {
    protected Level2AH(AQueryHelper helper) {
        super(helper);
    }

    @Sql("UPDATE nothing")
    protected abstract void updateNothingAgain() throws StorageException;

}
