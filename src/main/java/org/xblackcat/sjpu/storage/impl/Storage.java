package org.xblackcat.sjpu.storage.impl;

import javassist.ClassClassPath;
import javassist.ClassPool;
import org.xblackcat.sjpu.builder.ClassBuilder;
import org.xblackcat.sjpu.builder.Definer;
import org.xblackcat.sjpu.builder.FunctionalClassBuilder;
import org.xblackcat.sjpu.builder.InstanceClassCachedFactory;
import org.xblackcat.sjpu.storage.IStorage;
import org.xblackcat.sjpu.storage.ITx;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.StorageUtils;
import org.xblackcat.sjpu.storage.ann.Sql;
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
    private static class TypeMapperPoolHolder {
        private static final ClassPool POOL;

        static {
            final ClassLoader classLoader = new ClassLoader(Storage.class.getClassLoader()) {
            };
            POOL = new ClassPool(true) {
                @Override
                public ClassLoader getClassLoader() {
                    return classLoader;
                }
            };
            for (Class<?> cl : new Class<?>[]{AnAH.class, AFunctionalAH.class, IConnectionFactory.class, String.class}) {
                POOL.appendClassPath(new ClassClassPath(cl));
            }

        }
    }

    public Storage(IConnectionFactory connectionFactory, IMapFactory<?, ?>... mappers) {
        this(connectionFactory, StorageUtils.DEFAULT_ROWSET_CONSUMERS, mappers);
    }

    private static TypeMapper buildTypeMapper(IMapFactory<?, ?>[] mappers) {
        return new TypeMapper(TypeMapperPoolHolder.POOL, mappers);
    }

    public Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            IMapFactory<?, ?>... mappers
    ) {
        this(connectionFactory, rowSetConsumers, buildTypeMapper(mappers));
    }

    private Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            TypeMapper typeMapper
    ) {
        super(
                connectionFactory,
                typeMapper,
                new ClassBuilder<>(
                        new Definer<>(typeMapper.getParentPool(), AnAH.class, IConnectionFactory.class),
                        new SqlAnnotatedBuilder(typeMapper, rowSetConsumers),
                        new DDLAnnotatedBuilder(typeMapper.getParentPool())
                ),
                new FunctionalClassBuilder<>(
                        new Definer<>(typeMapper.getParentPool(), AFunctionalAH.class, IConnectionFactory.class, String.class),
                        new FunctionalAHBuilder(typeMapper, rowSetConsumers)
                ),
                new InstanceClassCachedFactory<>(
                        new ClassBuilder<>(
                                new Definer<>(typeMapper.getParentPool(), ABatchedAH.class, IConnectionFactory.class),
                                new BatchedSqlAnnotatedBuilder(typeMapper, rowSetConsumers),
                                new DDLAnnotatedBuilder(typeMapper.getParentPool()),
                                new CloseResourcesAnnotatedBuilder(typeMapper.getParentPool(), Sql.class)
                        ),
                        IConnectionFactory.class
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
            return new TxFactory(
                    connectionFactory,
                    transactionIsolationLevel,
                    typeMapper,
                    commonBuilder,
                    functionalBuilder,
                    batchedFactory
            );
        } catch (SQLException e) {
            throw new StorageException("An exception occurs while starting a transaction", e);
        }
    }

    @Override
    public void shutdown() throws StorageException {
        connectionFactory.shutdown();
    }
}
