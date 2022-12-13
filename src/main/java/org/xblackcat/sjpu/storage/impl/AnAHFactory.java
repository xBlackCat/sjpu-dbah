package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.builder.IBuilder;
import org.xblackcat.sjpu.builder.IFactory;
import org.xblackcat.sjpu.builder.InstanceCachedFactory;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IAHFactory;
import org.xblackcat.sjpu.storage.IBatchedAH;
import org.xblackcat.sjpu.storage.IFunctionalAH;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

/**
 * 15.11.13 14:22
 *
 * @author xBlackCat
 */
abstract class AnAHFactory implements IAHFactory {
    protected final IFactory<IAH> commonFactory;
    protected final IFactory<IFunctionalAH> functionalFactory;
    protected final IFactory<IBatchedAH> batchedFactory;

    protected final IConnectionFactory connectionFactory;
    protected final TypeMapper typeMapper;
    protected final IBuilder<IAH> commonBuilder;
    protected final IBuilder<IFunctionalAH> functionalBuilder;

    AnAHFactory(
            IConnectionFactory connectionFactory,
            TypeMapper typeMapper,
            IBuilder<IAH> commonBuilder,
            IBuilder<IFunctionalAH> functionalBuilder,
            IFactory<IBatchedAH> batchedFactory
    ) {
        this.connectionFactory = connectionFactory;
        this.typeMapper = typeMapper;
        this.commonBuilder = commonBuilder;
        this.functionalBuilder = functionalBuilder;

        commonFactory = new InstanceCachedFactory<>(commonBuilder, IConnectionFactory.class);
        functionalFactory = new InstanceCachedFactory<>(functionalBuilder, IConnectionFactory.class, String.class);
        this.batchedFactory = batchedFactory;
    }

    @Override
    final public <I extends IAH> I get(Class<I> clazz) {
        return commonFactory.get(clazz, connectionFactory);
    }

    @Override
    final public <T extends IFunctionalAH> T get(Class<T> functionalAH, String sql) {
        return functionalFactory.get(functionalAH, connectionFactory, sql);
    }

    @Override
    final public <T extends IBatchedAH> T startBatch(Class<T> batched) {
        return batchedFactory.get(batched, connectionFactory);
    }
}
