package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IAHFactory;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.skel.AFactory;
import org.xblackcat.sjpu.storage.skel.IBuilder;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

/**
 * 15.11.13 14:22
 *
 * @author xBlackCat
 */
abstract class AnAHFactory extends AFactory<IAH, IConnectionFactory> implements IAHFactory {
    protected final TypeMapper typeMapper;

    AnAHFactory(
            IConnectionFactory connectionFactory,
            TypeMapper typeMapper,
            IBuilder<IAH, IConnectionFactory> methodBuilder
    ) {
        super(connectionFactory, methodBuilder);
        this.typeMapper = typeMapper;
    }
}
