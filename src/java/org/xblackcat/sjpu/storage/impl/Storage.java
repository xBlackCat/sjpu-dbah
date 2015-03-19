package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.skel.Definer;
import org.xblackcat.sjpu.skel.MethodBuilder;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
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

    public Storage(IConnectionFactory connectionFactory, IMapFactory<?, ?>... mappers) {
        this(connectionFactory, StorageUtils.DEFAULT_ROWSET_CONSUMERS, mappers);
    }

    public Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            IMapFactory<?, ?>... mappers
    ) {
        this(connectionFactory, rowSetConsumers, new Definer<>(AnAH.class, IConnectionFactory.class), mappers);
    }

    private Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Definer<IAH, IConnectionFactory> definer,
            IMapFactory<?, ?>... mappers
    ) {
        this(
                connectionFactory,
                rowSetConsumers,
                definer,
                new TypeMapper(definer.getPool(), mappers)
        );
    }

    private Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Definer<IAH, IConnectionFactory> definer,
            TypeMapper typeMapper
    ) {
        super(
                connectionFactory,
                typeMapper,
                new MethodBuilder<>(
                        definer,
                        new SqlAnnotatedBuilder(typeMapper, rowSetConsumers),
                        new DDLAnnotatedBuilder(typeMapper.getParentPool())
                )
        );
    }

    @Override
    public ITx beginTransaction() throws StorageException {
        return beginTransaction(-1);
    }

    @Override
    public ITx beginTransaction(int transactionIsolationLevel) throws StorageException {
        try {
            return new TxFactory(factory, transactionIsolationLevel, typeMapper, builder);
        } catch (SQLException e) {
            throw new StorageException("An exception occurs while starting a transaction", e);
        }
    }

    @Override
    public void shutdown() throws StorageException {
        factory.shutdown();
    }
}
