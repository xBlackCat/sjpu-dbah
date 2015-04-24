package org.xblackcat.sjpu.storage.impl;

import javassist.ClassClassPath;
import javassist.ClassPool;
import org.xblackcat.sjpu.skel.ClassBuilder;
import org.xblackcat.sjpu.skel.Definer;
import org.xblackcat.sjpu.skel.FunctionalClassBuilder;
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

    private static ClassPool createPool(Class<?>... paramClasses) {
        final ClassLoader classLoader = new ClassLoader(Storage.class.getClassLoader()) {
        };
        ClassPool pool = new ClassPool(true) {
            @Override
            public ClassLoader getClassLoader() {
                return classLoader;
            }
        };
        for (Class<?> cl : paramClasses) {
            pool.appendClassPath(new ClassClassPath(cl));
        }

        return pool;
    }

    public Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            IMapFactory<?, ?>... mappers
    ) {
        this(
                connectionFactory,
                rowSetConsumers,
                createPool(AnAH.class, AFunctionalAH.class, IConnectionFactory.class, String.class),
                mappers
        );
    }

    private Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            ClassPool pool,
            IMapFactory<?, ?>... mappers
    ) {
        this(
                connectionFactory,
                rowSetConsumers,
                new Definer<>(pool, AnAH.class, IConnectionFactory.class),
                new Definer<>(pool, AFunctionalAH.class, IConnectionFactory.class, String.class),
                new TypeMapper(pool, mappers)
        );
    }

    private Storage(
            IConnectionFactory connectionFactory,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Definer<IAH> definer,
            Definer<IFunctionalAH> definerF,
            TypeMapper typeMapper
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
            return new TxFactory(factory, transactionIsolationLevel, typeMapper, commonBuilder, functionalBuilder);
        } catch (SQLException e) {
            throw new StorageException("An exception occurs while starting a transaction", e);
        }
    }

    @Override
    public void shutdown() throws StorageException {
        factory.shutdown();
    }
}
