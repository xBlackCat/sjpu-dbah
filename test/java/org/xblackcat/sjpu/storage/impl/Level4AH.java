package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

/**
 * 13.12.13 16:52
 *
 * @author xBlackCat
 */
public abstract class Level4AH extends Level3AH {
    protected Level4AH(IConnectionFactory helper) {
        super(helper);
    }

    @Override
    protected void updateNothingMore() throws StorageException {

    }

    @Override
    public void updateNothingAgain() throws StorageException {

    }
}
