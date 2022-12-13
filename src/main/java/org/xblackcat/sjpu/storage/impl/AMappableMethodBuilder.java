package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.builder.AnAnnotatedMethodBuilder;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 19.12.13 16:50
 *
 * @author xBlackCat
 */
public abstract class AMappableMethodBuilder<A extends Annotation> extends AnAnnotatedMethodBuilder<A> {
    protected final TypeMapper typeMapper;
    protected final Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers;

    public AMappableMethodBuilder(
            Class<A> annClass,
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers
    ) {
        super(annClass);
        this.typeMapper = typeMapper;
        this.rowSetConsumers = rowSetConsumers;
    }

    protected Class<? extends IRowSetConsumer> hasRowSetConsumer(Class<?> returnType, Class<?> realReturnType) {
        for (Map.Entry<Class<?>, Class<? extends IRowSetConsumer>> e : rowSetConsumers.entrySet()) {
            if (returnType.equals(e.getKey()) && !realReturnType.isAssignableFrom(e.getKey())) {
                return e.getValue();
            }
        }

        return null;
    }
}
