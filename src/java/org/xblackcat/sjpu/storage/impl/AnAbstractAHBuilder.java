package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 21.02.13 11:54
 *
 * @author xBlackCat
 */
abstract class AnAbstractAHBuilder extends InstanceAHBuilder {
    private static final Map<Class<? extends Annotation>, IMethodBuilder> METHOD_BUILDERS;

    static {
        METHOD_BUILDERS = new HashMap<>();

        METHOD_BUILDERS.put(Sql.class, new SqlAnnotatedBuilder());
        METHOD_BUILDERS.put(GetObject.class, new GetObjectAnnotatedBuilder());
        METHOD_BUILDERS.put(UpdateObject.class, new UpdateObjectAnnotatedBuilder());
    }

    protected final ClassPool pool;
    protected final Log log = LogFactory.getLog(getClass());

    protected AnAbstractAHBuilder() {
        pool = ClassPool.getDefault();
    }

    @Override
    public <T extends IAH> T build(Class<T> target, IQueryHelper helper) throws StorageSetupException {
        try {
            final CtClass accessHelper = defineCtClass(target);

            final CtConstructor constructor = CtNewConstructor.make(
                    new CtClass[]{pool.get(IQueryHelper.class.getName())},
                    BuilderUtils.EMPTY_LIST,
                    "{ super($1); }",
                    accessHelper
            );

            accessHelper.addConstructor(constructor);

            try {
                for (Method m : target.getMethods()) {
                    implementMethod(accessHelper, m);
                }
            } catch (NoSuchMethodException e) {
                throw new StorageSetupException("Can't find a method in implementing class", e);
            }

            @SuppressWarnings("unchecked")
            final Class<T> ahClass = (Class<T>) accessHelper.toClass();
            return super.build(ahClass, helper);
        } catch (NotFoundException | CannotCompileException e) {
            throw new StorageSetupException("Exception", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void implementMethod(
            CtClass accessHelper,
            Method m
    ) throws NotFoundException, NoSuchMethodException, CannotCompileException {
        if (log.isTraceEnabled()) {
            log.trace("Check method: " + m);
        }

        if (!Modifier.isAbstract(m.getModifiers())) {
            if (log.isTraceEnabled()) {
                log.trace("Method already implemented - skip it");
            }

            return;
        }

        for (Map.Entry<Class<? extends Annotation>, IMethodBuilder> builder : METHOD_BUILDERS.entrySet()) {
            final Annotation annotation = m.getAnnotation(builder.getKey());

            if (annotation != null) {
                builder.getValue().buildMethod(pool, accessHelper, m, annotation);

                return;
            }
        }

        throw new StorageSetupException(
                "Method " +
                        m +
                        " should be annonated with one of the following annotations:  " +
                        METHOD_BUILDERS.keySet()
        );
    }

    protected abstract <T extends IAH> CtClass defineCtClass(Class<T> target) throws NotFoundException, CannotCompileException;

}
