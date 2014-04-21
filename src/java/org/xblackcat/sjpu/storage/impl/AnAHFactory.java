package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IAHFactory;
import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.skel.AFactory;
import org.xblackcat.sjpu.storage.skel.Definer;
import org.xblackcat.sjpu.storage.skel.IBuilder;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.util.Map;

/**
 * 15.11.13 14:22
 *
 * @author xBlackCat
 */
abstract class AnAHFactory extends AFactory<IAH, IQueryHelper> implements IAHFactory {
    protected final TypeMapper typeMapper;
    protected final Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers;

    AnAHFactory(
            Definer<IAH, IQueryHelper> definer,
            IQueryHelper queryHelper,
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            IBuilder<IAH, IQueryHelper> methodBuilder
    ) {
        super(definer, queryHelper, methodBuilder);

        if (rowSetConsumers == null) {
            throw new NullPointerException("RowSet consumer map can't be null");
        }
        this.typeMapper = typeMapper;
        this.rowSetConsumers = rowSetConsumers;
    }
}
