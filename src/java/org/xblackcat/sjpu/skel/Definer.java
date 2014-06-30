package org.xblackcat.sjpu.skel;

import javassist.*;

import java.lang.reflect.InvocationTargetException;

/**
 * 15.11.13 15:37
 *
 * @author xBlackCat
 */
public class Definer<Base, Helper> {
    private final Class<? extends Base> baseClass;
    private final Class<Helper> paramClass;
    private final ClassPool pool;
    private final ClassLoader classLoader = new ClassLoader(Definer.class.getClassLoader()) {
    };

    public Definer(Class<? extends Base> baseClass, Class<Helper> paramClass) {
        this.baseClass = baseClass;
        this.paramClass = paramClass;
        pool = new ClassPool(true) {
            @Override
            public ClassLoader getClassLoader() {
                return classLoader;
            }
        };
        pool.appendClassPath(new ClassClassPath(baseClass));
        pool.appendClassPath(new ClassClassPath(paramClass));
    }

    protected String getParamClassName() {
        return paramClass.getName();
    }

    public String getBaseClassName() {
        return baseClass.getName();
    }

    protected boolean isAssignable(Class<?> clazz) {
        return baseClass.isAssignableFrom(clazz);
    }

    public String getNestedClassName() {
        return baseClass.getSimpleName() + "Impl";
    }

    protected <T extends Base> T build(Class<T> target, Helper param) throws GeneratorException {
        try {
            return target.getConstructor(paramClass).newInstance(param);
        } catch (InstantiationException e) {
            throw new GeneratorException("Class is not implemented", e);
        } catch (IllegalAccessException e) {
            throw new GeneratorException("Access helper constructor should be public", e);
        } catch (InvocationTargetException e) {
            throw new GeneratorException("Exception occurs in access helper constructor", e);
        } catch (NoSuchMethodException e) {
            throw new GeneratorException(
                    "Access helper class constructor should have the following signature: " +
                            target.getName() + "(" + paramClass.getName() + " arg);",
                    e
            );
        }

    }

    public CtConstructor buildCtConstructor(CtClass accessHelper) throws NotFoundException, CannotCompileException {
        return CtNewConstructor.make(
                new CtClass[]{pool.get(getParamClassName())},
                BuilderUtils.EMPTY_LIST,
                "{ super($1); }",
                accessHelper
        );
    }

    public ClassPool getPool() {
        return pool;
    }
}
