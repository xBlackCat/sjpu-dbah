package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.skel.AFactory;
import org.xblackcat.sjpu.skel.IBuilder;
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
abstract class AnAHFactory extends AFactory<IAH, IConnectionFactory> implements IAHFactory {
    protected final TypeMapper typeMapper;
    protected final IBuilder<IFunctionalAH, IConnectionFactory> functionalBuilder;

    AnAHFactory(
            IConnectionFactory connectionFactory,
            TypeMapper typeMapper,
            IBuilder<IAH, IConnectionFactory> methodBuilder,
            IBuilder<IFunctionalAH, IConnectionFactory> functionalBuilder
    ) {
        super(connectionFactory, methodBuilder);
        this.typeMapper = typeMapper;
        this.functionalBuilder = functionalBuilder;
    }
}
