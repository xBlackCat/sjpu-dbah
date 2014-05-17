package org.xblackcat.sjpu.storage.skel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 19.12.13 16:50
 *
 * @author xBlackCat
 */
public abstract class AMethodBuilder<T extends Annotation> implements IMethodBuilder<T> {
    protected final Log log = LogFactory.getLog(getClass());
    protected final TypeMapper typeMapper;
    protected final Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers;
    private final Class<T> annClass;

    public AMethodBuilder(
            Class<T> annClass,
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
    public Class<T> getAnnotationClass() {
        return annClass;
    }
}
