package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageSetupException;

import java.lang.reflect.InvocationTargetException;

/**
 * 15.11.13 15:37
 *
 * @author xBlackCat
 */
class Definer<B, P> {
    static final Definer<AnAH, IQueryHelper> DEFAULT_DEFINER = new Definer<>(AnAH.class, IQueryHelper.class);

    private final Class<B> baseClass;
    private final Class<P> paramClass;
    private final ClassPool pool;

    protected Definer(Class<B> baseClass, Class<P> paramClass) {
        this.baseClass = baseClass;
        this.paramClass = paramClass;
        pool = new ClassPool(true);
        pool.appendClassPath(new ClassClassPath(IAH.class));
        pool.appendClassPath(new ClassClassPath(baseClass));
        pool.appendClassPath(new ClassClassPath(paramClass));
    }

    protected String getParamClassName() {
        return paramClass.getName();
    }

    protected String getBaseClassName() {
        return baseClass.getName();
    }

    protected boolean isAssignable(Class<?> clazz) {
        return baseClass.isAssignableFrom(clazz);
    }

    protected String getNestedClassName() {
        return baseClass.getSimpleName() + "Impl";
    }

    protected <T extends IAH> T build(Class<T> target, P param) throws StorageSetupException {
        try {
            return target.getConstructor(paramClass).newInstance(param);
        } catch (InstantiationException e) {
            throw new StorageSetupException("Class is not implemented", e);
        } catch (IllegalAccessException e) {
            throw new StorageSetupException("Access helper constructor should be public", e);
        } catch (InvocationTargetException e) {
            throw new StorageSetupException("Exception occurs in access helper constructor", e);
        } catch (NoSuchMethodException e) {
            throw new StorageSetupException(
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
