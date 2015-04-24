package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.skel.IBuilder;
import org.xblackcat.sjpu.skel.IFactory;
import org.xblackcat.sjpu.skel.InstanceFactory;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IAHFactory;
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

    protected final IConnectionFactory factory;
    protected final TypeMapper typeMapper;
    protected final IBuilder<IAH> commonBuilder;
    protected final IBuilder<IFunctionalAH> functionalBuilder;

    AnAHFactory(
            IConnectionFactory factory,
            TypeMapper typeMapper,
            IBuilder<IAH> commonBuilder,
            IBuilder<IFunctionalAH> functionalBuilder
    ) {
        this.factory = factory;
        this.typeMapper = typeMapper;
        this.commonBuilder = commonBuilder;
        this.functionalBuilder = functionalBuilder;
        commonFactory = new InstanceFactory<>(commonBuilder, IConnectionFactory.class);
        functionalFactory = new InstanceFactory<>(functionalBuilder, IConnectionFactory.class, String.class);
    }

    @Override
    final public <I extends IAH> I get(Class<I> clazz) {
        return commonFactory.get(clazz, factory);
    }

    @Override
    final public <T extends IFunctionalAH> T get(Class<T> functionalAH, String sql) {
        return functionalFactory.get(functionalAH, factory, sql);
    }
}
