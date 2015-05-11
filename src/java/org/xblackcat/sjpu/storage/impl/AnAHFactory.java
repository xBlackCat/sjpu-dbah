package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.skel.IBuilder;
import org.xblackcat.sjpu.skel.IFactory;
import org.xblackcat.sjpu.skel.InstanceCachedFactory;
import org.xblackcat.sjpu.skel.InstanceFactory;
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
    protected final IBuilder<IBatchedAH> batchedBuilder;

    AnAHFactory(
            IConnectionFactory connectionFactory,
            TypeMapper typeMapper,
            IBuilder<IAH> commonBuilder,
            IBuilder<IFunctionalAH> functionalBuilder,
            IBuilder<IBatchedAH> batchedBuilder
    ) {
        this.connectionFactory = connectionFactory;
        this.typeMapper = typeMapper;
        this.commonBuilder = commonBuilder;
        this.functionalBuilder = functionalBuilder;
        this.batchedBuilder = batchedBuilder;

        commonFactory = new InstanceCachedFactory<>(commonBuilder, IConnectionFactory.class);
        functionalFactory = new InstanceCachedFactory<>(functionalBuilder, IConnectionFactory.class, String.class);
        batchedFactory = new InstanceFactory<>(batchedBuilder, IConnectionFactory.class);
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
