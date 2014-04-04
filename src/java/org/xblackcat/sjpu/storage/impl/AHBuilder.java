package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 21.02.13 11:54
 *
 * @author xBlackCat
 */
class AHBuilder<B, P> implements IAHBuilder<P> {
    private final Map<Class<? extends Annotation>, IMethodBuilder> methodBuilders = new LinkedHashMap<>();

    protected final Definer<B, P> definer;
    protected final Log log = LogFactory.getLog(getClass());

    protected AHBuilder(TypeMapper typeMapper, Definer<B, P> definer, Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers) {
        this.definer = definer;

        methodBuilders.put(Sql.class, new SqlAnnotatedBuilder(typeMapper, rowSetConsumers));
    }

    @Override
    public <T extends IAH> T build(Class<T> target, P helper) throws StorageSetupException {
        try {
            // For the first: check if the implementation is exists

            try {
                Class<?> clazz = Class.forName(target.getName() + "$" + definer.getNestedClassName());

                if (!target.isAssignableFrom(clazz)) {
                    throw new StorageSetupException(
                            target.getName() +
                                    " already have implemented inner class " +
                                    definer.getNestedClassName() +
                                    " with inconsistent structure."
                    );
                }

                @SuppressWarnings("unchecked") Class<T> aClass = (Class<T>) clazz;
                return definer.build(aClass, helper);
            } catch (ClassNotFoundException e) {
                // Ignore, smile and go further
            }

            // Class not yet built: create a new one
            final CtClass accessHelper;
            if (target.isInterface()) {
                accessHelper = defineCtClassByInterface(target);
            } else {
                if (!definer.isAssignable(target)) {
                    throw new StorageSetupException("Access helper class should have " + definer.getBaseClassName() + " as super class");
                }
                if (Modifier.isAbstract(target.getModifiers())) {
                    accessHelper = defineCtClassByAbstract(target);
                } else {
                    return definer.build(target, helper);
                }
            }

            final CtConstructor constructor = definer.buildCtConstructor(accessHelper);

            accessHelper.addConstructor(constructor);

            for (Method m : target.getMethods()) {
                implementMethod(accessHelper, m);
            }

            Set<ImplementedMethod> implementedMethods = new HashSet<>();
            // Implement protected and other methods
            implementNotPublicMethods(target, target, accessHelper, implementedMethods);

            @SuppressWarnings("unchecked")
            final Class<T> ahClass = (Class<T>) accessHelper.toClass();
            return definer.build(ahClass, helper);
        } catch (NotFoundException | CannotCompileException e) {
            throw new StorageSetupException("Exception occurs while build AccessHelper", e);
        }
    }

    private void implementNotPublicMethods(
            Class<?> root,
            Class<?> target,
            CtClass accessHelper,
            Set<ImplementedMethod> implementedMethods
    ) throws StorageSetupException {
        if (target == null || target == Object.class) {
            // Done
            return;
        }

        for (Method m : target.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                // Public methods already checked
                continue;
            }

            final ImplementedMethod method = new ImplementedMethod(m.getName(), m.getParameterTypes());
            if (!Modifier.isAbstract(m.getModifiers())) {
                implementedMethods.add(method);
                continue;
            }

            try {
                // Check non-public abstract method for implementation in the root class
                root.getMethod(m.getName(), m.getParameterTypes());
            } catch (NoSuchMethodException e) {
                // Method is not found - build it!

                if (implementedMethods.add(method)) {
                    implementMethod(accessHelper, m);
                }
            }
        }

        implementNotPublicMethods(root, target.getSuperclass(), accessHelper, implementedMethods);
    }

    private <T extends IAH> CtClass defineCtClassByInterface(Class<T> target) throws NotFoundException, CannotCompileException {
        ClassPool pool = BuilderUtils.getClassPool(definer.getPool(), target);
        pool.appendClassPath(new ClassClassPath(target));

        CtClass baseClass = pool.get(target.getName());
        final CtClass accessHelper = baseClass.makeNestedClass(definer.getNestedClassName(), true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.addInterface(pool.get(target.getName()));
        accessHelper.setSuperclass(pool.get(definer.getBaseClassName()));

        return accessHelper;
    }

    private <T extends IAH> CtClass defineCtClassByAbstract(Class<T> target) throws NotFoundException, CannotCompileException {
        ClassPool pool = BuilderUtils.getClassPool(definer.getPool(), target);

        CtClass thisClass = pool.get(target.getName());
        CtClass accessHelper = thisClass.makeNestedClass(definer.getNestedClassName(), true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.setSuperclass(pool.get(target.getName()));
        return accessHelper;
    }

    @SuppressWarnings("unchecked")
    private void implementMethod(CtClass accessHelper, Method m) throws StorageSetupException {
        if (log.isTraceEnabled()) {
            log.trace("Check method: " + m);
        }

        if (!Modifier.isAbstract(m.getModifiers())) {
            if (log.isTraceEnabled()) {
                log.trace("Method already implemented - skip it");
            }

            return;
        }

        for (Map.Entry<Class<? extends Annotation>, IMethodBuilder> builder : methodBuilders.entrySet()) {
            final Annotation annotation = m.getAnnotation(builder.getKey());

            if (annotation != null) {
                try {
                    builder.getValue().buildMethod(accessHelper, m, annotation);
                } catch (NotFoundException | CannotCompileException | ReflectiveOperationException e) {
                    throw new StorageSetupException("Exception occurs while building method " + m, e);
                } catch (StorageSetupException e) {
                    final StorageSetupException ex = new StorageSetupException(
                            "Exception occurs while building method " +
                                    m +
                                    ": " +
                                    e.getMessage(), e.getCause()
                    );
                    ex.setStackTrace(e.getStackTrace());
                    throw ex;
                }

                return;
            }
        }

        throw new StorageSetupException(
                "Method " + m + " should be annotated with one of the following annotations:  " + methodBuilders.keySet()
        );
    }

    private final static class ImplementedMethod {
        private final String name;
        private final Class<?>[] parameters;

        private ImplementedMethod(String name, Class<?>[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ImplementedMethod)) {
                return false;
            }

            ImplementedMethod that = (ImplementedMethod) o;

            return name.equals(that.name) && Arrays.equals(parameters, that.parameters);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + Arrays.hashCode(parameters);
            return result;
        }
    }

}
