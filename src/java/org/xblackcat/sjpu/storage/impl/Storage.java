package org.xblackcat.sjpu.storage.impl;

import javassist.ClassClassPath;
import javassist.ClassPool;
import org.xblackcat.sjpu.skel.ClassBuilder;
import org.xblackcat.sjpu.skel.Definer;
import org.xblackcat.sjpu.skel.FunctionalClassBuilder;
import org.xblackcat.sjpu.skel.InstanceClassCachedFactory;
import org.xblackcat.sjpu.storage.*;
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
    public Storage(IConnectionFactory connectionFactory, IMapFactory<?, ?>... mappers) {
        this(connectionFactory, StorageUtils.DEFAULT_ROWSET_CONSUMERS, mappers);
    }

    private static TypeMapper buildTypeMapper(IMapFactory<?, ?>[] mappers) {
        final ClassLoader classLoader = new ClassLoader(Storage.class.getClassLoader()) {
        };
        ClassPool pool = new ClassPool(true) {
            @Override
            public ClassLoader getClassLoader() {
                return classLoader;
            }
        };
        for (Class<?> cl : new Class<?>[]{AnAH.class, AFunctionalAH.class, IConnectionFactory.class, String.class}) {
            pool.appendClassPath(new ClassClassPath(cl));
        }

        return new TypeMapper(pool, mappers);
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
        this(
                connectionFactory,
                rowSetConsumers,
                typeMapper,
                new Definer<>(typeMapper.getParentPool(), AnAH.class, IConnectionFactory.class),
                new Definer<>(typeMapper.getParentPool(), AFunctionalAH.class, IConnectionFactory.class, String.class),
                new Definer<>(typeMapper.getParentPool(), ABatchedAH.class, IConnectionFactory.class)
        );
    }

    private Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            TypeMapper typeMapper,
            Definer<IAH> definer,
            Definer<IFunctionalAH> definerF,
            Definer<IBatchedAH> definerB
    ) {
        super(
                connectionFactory,
                typeMapper,
                new ClassBuilder<>(
                        definer,
                        new SqlAnnotatedBuilder(typeMapper, rowSetConsumers),
                        new DDLAnnotatedBuilder(typeMapper.getParentPool())
                ),
                new FunctionalClassBuilder<>(
                        definerF,
                        new FunctionalAHBuilder(typeMapper, rowSetConsumers)
                ),
                new InstanceClassCachedFactory<>(
                        new ClassBuilder<>(
                                definerB,
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
