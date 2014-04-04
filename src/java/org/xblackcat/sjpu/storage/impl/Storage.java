package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.skel.Definer;
import org.xblackcat.sjpu.storage.typemap.IMapFactory;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.sql.SQLException;
import java.util.Map;

/**
 * 30.01.12 12:47
 *
 * @author xBlackCat
 */
public class Storage extends AnAHFactory implements IStorage {
    private static final Definer<IAH, IQueryHelper> DEFAULT_DEFINER = new Definer<IAH, IQueryHelper>(AnAH.class, IQueryHelper.class);

    public Storage(IQueryHelper queryHelper, IMapFactory<?, ?>... mappers) {
        this(queryHelper, StorageUtils.DEFAULT_ROWSET_CONSUMERS, mappers);
    }

    public Storage(
            IQueryHelper queryHelper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            IMapFactory<?, ?>... mappers
    ) {
        this(queryHelper, rowSetConsumers, DEFAULT_DEFINER, mappers);
    }

    private Storage(
            IQueryHelper queryHelper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Definer<IAH, IQueryHelper> definer,
            IMapFactory<?, ?>... mappers
    ) {
        super(definer, queryHelper, new TypeMapper(definer.getPool(), mappers), rowSetConsumers);
    }

    @Override
    public IBatch openTransaction() throws StorageException {
        return openTransaction(-1);
    }

    @Override
    public IBatch openTransaction(int transactionIsolationLevel) throws StorageException {
        try {
            return new BatchHelper(queryHelper, transactionIsolationLevel, definer, typeMapper, rowSetConsumers);
        } catch (SQLException e) {
            throw new StorageException("An exception occurs while starting a transaction", e);
        }
    }

    @Override
    public void shutdown() throws StorageException {
        queryHelper.shutdown();
    }
}
