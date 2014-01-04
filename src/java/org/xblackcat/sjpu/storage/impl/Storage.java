package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IBatch;
import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.IStorage;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.typemap.IMapFactory;

import java.sql.SQLException;

/**
 * 30.01.12 12:47
 *
 * @author xBlackCat
 */
public class Storage extends AnAHFactory implements IStorage {
    public Storage(IQueryHelper queryHelper, IMapFactory<?, ?>... mappers) {
        super(queryHelper, new TypeMapper(mappers));
    }

    @Override
    public IBatch openTransaction() throws StorageException {
        return openTransaction(-1);
    }

    @Override
    public IBatch openTransaction(int transactionIsolationLevel) throws StorageException {
        try {
            return new BatchHelper(queryHelper, transactionIsolationLevel, typeMapper);
        } catch (SQLException e) {
            throw new StorageException("An exception occurs while starting a transaction", e);
        }
    }

    @Override
    public void shutdown() throws StorageException {
        queryHelper.shutdown();
    }
}
