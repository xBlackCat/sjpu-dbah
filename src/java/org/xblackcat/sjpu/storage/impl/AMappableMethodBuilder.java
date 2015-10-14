package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.skel.IMethodBuilder;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 19.12.13 16:50
 *
 * @author xBlackCat
 */
public abstract class AMappableMethodBuilder<A extends Annotation> implements IMethodBuilder<A> {
    protected final Log log = LogFactory.getLog(getClass());
    protected final TypeMapper typeMapper;
    protected final Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers;
    private final Class<A> annClass;

    public AMappableMethodBuilder(
            Class<A> annClass,
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers
    ) {
        this.typeMapper = typeMapper;
        this.rowSetConsumers = rowSetConsumers;
        this.annClass = annClass;
    }

    protected Class<? extends IRowSetConsumer> hasRowSetConsumer(Class<?> returnType, Class<?> realReturnType) {
        for (Map.Entry<Class<?>, Class<? extends IRowSetConsumer>> e : rowSetConsumers.entrySet()) {
            if (returnType.equals(e.getKey()) && !realReturnType.isAssignableFrom(e.getKey())) {
                return e.getValue();
            }
        }

        return null;
    }

    @Override
    public Class<A> getAnnotationClass() {
        return annClass;
    }

    protected A getAnnotation(Method m) {
        return m.getAnnotation(getAnnotationClass());
    }
}
