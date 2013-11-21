package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IBatch;
import org.xblackcat.sjpu.storage.IStorage;
import org.xblackcat.sjpu.storage.StorageException;

import java.sql.SQLException;

/**
 * 30.01.12 12:47
 *
 * @author xBlackCat
 */
public class Storage extends AnAHFactory implements IStorage {
    public Storage(AQueryHelper queryHelper) {
        super(queryHelper);
    }

    @Override
    public IBatch openTransaction() throws StorageException {
        try {
            return new BatchHelper(queryHelper);
        } catch (SQLException e) {
            throw new StorageException("An exception occurs while starting a transaction", e);
        }
    }
}
