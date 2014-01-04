package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 13.12.13 16:52
 *
 * @author xBlackCat
 */
public abstract class Level4AH extends Level3AH {
    protected Level4AH(IQueryHelper helper) {
        super(helper);
    }

    @Override
    protected void updateNothingMore() throws StorageException {

    }

    @Override
    protected void updateNothingAgain() throws StorageException {

    }
}
