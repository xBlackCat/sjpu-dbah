package org.xblackcat.sjpu.skel;

import javassist.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 04.04.2014 16:10
 *
 * @author xBlackCat
 */
public class ClassBuilder<Base> implements IBuilder<Base> {
    private final Map<Class<? extends Annotation>, IMethodBuilder> methodBuilders = new LinkedHashMap<>();
    private final IDefiner<Base> definer;

    protected final Log log = LogFactory.getLog(getClass());

    public ClassBuilder(IDefiner<Base> definer, IMethodBuilder<?>... builders) {
        this.definer = definer;

        for (IMethodBuilder<?> builder : builders) {
            methodBuilders.put(builder.getAnnotationClass(), builder);
        }
    }

    public <T extends Base> Class<? extends T> build(Class<T> target) throws GeneratorException {
        try {
            // For the first: check if the implementation is exists

            try {
                Class<?> clazz = BuilderUtils.getClass(target.getName() + "$" + definer.getNestedClassName(), definer.getPool());

                if (!target.isAssignableFrom(clazz)) {
                    throw new GeneratorException(
                            target.getName() + " already have implemented inner class " +
                                    definer.getNestedClassName() + " with inconsistent structure."
                    );
                }

                @SuppressWarnings("unchecked") Class<T> aClass = (Class<T>) clazz;
                return aClass;
            } catch (ClassNotFoundException e) {
                // Ignore, smile and go further
            }

            // Class not yet built: create a new one
            final CtClass accessHelper;
            if (target.isInterface()) {
                accessHelper = defineCtClassByInterface(target);
            } else {
                if (!definer.isAssignable(target)) {
                    throw new GeneratorException("Access helper class should have " + definer.getBaseCtClass() + " as super class");
                }
                if (Modifier.isAbstract(target.getModifiers())) {
                    accessHelper = defineCtClassByAbstract(target);
                } else {
                    return target;
                }
            }

            final CtConstructor constructor = definer.buildCtConstructor(accessHelper);

            accessHelper.addConstructor(constructor);

            for (Method m : target.getMethods()) {
                implementMethod(accessHelper, m);
            }

            Set<ImplementedMethod> implementedMethods = new HashSet<>();
            // Implement protected and other methods
            implementNonPublicMethods(target, target, accessHelper, implementedMethods);

            @SuppressWarnings("unchecked")
            final Class<T> ahClass = (Class<T>) accessHelper.toClass();
            return ahClass;
        } catch (NotFoundException | CannotCompileException e) {
            throw new GeneratorException("Exception occurs while build AccessHelper", e);
        }
    }

    private void implementNonPublicMethods(
            Class<?> root,
            Class<?> target,
            CtClass accessHelper,
            Set<ImplementedMethod> implementedMethods
    ) throws GeneratorException {
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

        implementNonPublicMethods(root, target.getSuperclass(), accessHelper, implementedMethods);
    }

    private <T extends Base> CtClass defineCtClassByInterface(Class<T> target) throws NotFoundException, CannotCompileException {
        ClassPool pool = BuilderUtils.getClassPool(definer.getPool(), target);
        pool.appendClassPath(new ClassClassPath(target));

        CtClass baseClass = pool.get(target.getName());
        final CtClass accessHelper = baseClass.makeNestedClass(definer.getNestedClassName(), true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.addInterface(baseClass);
        accessHelper.setSuperclass(definer.getBaseCtClass());

        return accessHelper;
    }

    private <T extends Base> CtClass defineCtClassByAbstract(Class<T> target) throws NotFoundException, CannotCompileException {
        ClassPool pool = BuilderUtils.getClassPool(definer.getPool(), target);

        CtClass thisClass = pool.get(target.getName());
        CtClass accessHelper = thisClass.makeNestedClass(definer.getNestedClassName(), true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.setSuperclass(pool.get(target.getName()));
        return accessHelper;
    }

    @SuppressWarnings("unchecked")
    private void implementMethod(CtClass accessHelper, Method m) throws GeneratorException {
        if (log.isTraceEnabled()) {
            log.trace("Check method: " + m);
        }

        if (!Modifier.isAbstract(m.getModifiers())) {
            if (log.isTraceEnabled()) {
                log.trace("Method already implemented - skip it");
            }

            return;
        }

        List<Class<? extends Annotation>> annotations =
                methodBuilders.keySet().stream().filter(m::isAnnotationPresent).collect(Collectors.toList());

        if (annotations.isEmpty()) {
            throw new GeneratorException(
                    "Method " + m + " should be annotated with one of the following annotations:  " + methodBuilders.keySet()
            );
        }

        if (annotations.size() > 1) {
            throw new GeneratorException(
                    "Method " + m + " should be annotated with ONLY one of the following annotations:  " + methodBuilders.keySet()
            );
        }

        try {
            methodBuilders.get(annotations.get(0)).buildMethod(accessHelper, m);
        } catch (NotFoundException | CannotCompileException | ReflectiveOperationException e) {
            throw new GeneratorException("Exception occurs while building method " + m, e);
        } catch (GeneratorException e) {
            final GeneratorException ex = new GeneratorException(
                    "Exception occurs while building method " + m + ": " + e.getMessage(), e.getCause()
            );
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
    }

    protected final static class ImplementedMethod {
        private final String name;
        private final Class<?>[] parameters;

        public ImplementedMethod(String name, Class<?>[] parameters) {
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
