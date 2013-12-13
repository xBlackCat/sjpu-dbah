package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageSetupException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 21.02.13 11:54
 *
 * @author xBlackCat
 */
class AHBuilder<B, P> implements IAHBuilder<P> {
    private static final Map<Class<? extends Annotation>, IMethodBuilder> METHOD_BUILDERS;

    static {
        METHOD_BUILDERS = new LinkedHashMap<>();

        METHOD_BUILDERS.put(Sql.class, new SqlAnnotatedBuilder());
//        METHOD_BUILDERS.put(GetObject.class, new GetObjectAnnotatedBuilder());
//        METHOD_BUILDERS.put(UpdateObject.class, new UpdateObjectAnnotatedBuilder());
    }

    protected final ClassPool pool;
    protected final Definer<B, P> definer;
    protected final Log log = LogFactory.getLog(getClass());

    protected AHBuilder(Definer<B, P> definer) {
        this.definer = definer;
        pool = ClassPool.getDefault();
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

            final CtConstructor constructor = CtNewConstructor.make(
                    new CtClass[]{pool.get(definer.getParamClassName())},
                    BuilderUtils.EMPTY_LIST,
                    "{ super($1); }",
                    accessHelper
            );

            accessHelper.addConstructor(constructor);

            try {
                for (Method m : target.getMethods()) {
                    implementMethod(accessHelper, m);
                }

                Set<ImplementedMethod> implementedMethods = new HashSet<>();
                // Implement protected and other methods
                implementNotPublicMethods(target, target, accessHelper, implementedMethods);
            } catch (NoSuchMethodException e) {
                throw new StorageSetupException("Can't find a method in implementing class", e);
            }

            @SuppressWarnings("unchecked")
            final Class<T> ahClass = (Class<T>) accessHelper.toClass();
            return definer.build(ahClass, helper);
        } catch (NotFoundException | CannotCompileException e) {
            throw new StorageSetupException("Exception", e);
        }
    }

    private void implementNotPublicMethods(
            Class<?> root,
            Class<?> target,
            CtClass accessHelper,
            Set<ImplementedMethod> implementedMethods
    ) throws NoSuchMethodException, CannotCompileException, NotFoundException {
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
        CtClass baseClass = pool.get(target.getName());
        final CtClass accessHelper = baseClass.makeNestedClass(definer.getNestedClassName(), true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.addInterface(pool.get(target.getName()));
        accessHelper.setSuperclass(pool.get(definer.getBaseClassName()));

        return accessHelper;
    }

    private <T extends IAH> CtClass defineCtClassByAbstract(Class<T> target) throws NotFoundException, CannotCompileException {
        CtClass thisClass = pool.get(target.getName());
        CtClass accessHelper = thisClass.makeNestedClass(definer.getNestedClassName(), true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.setSuperclass(pool.get(target.getName()));
        return accessHelper;
    }

    @SuppressWarnings("unchecked")
    private void implementMethod(CtClass accessHelper, Method m) throws NotFoundException, NoSuchMethodException, CannotCompileException {
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
                "Method " + m + " should be annotated with one of the following annotations:  " + METHOD_BUILDERS.keySet()
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
