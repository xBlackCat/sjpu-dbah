package org.xblackcat.sjpu.skel;

import javassist.*;

import java.lang.reflect.Method;

/**
 * 24.04.2015 12:34
 *
 * @author xBlackCat
 */
public interface IDefiner {
    CtClass getBaseCtClass() throws NotFoundException;

    boolean isAssignable(Class<?> clazz);

    boolean isImplemented(Method m);

    String getNestedClassName();

    CtConstructor buildCtConstructor(CtClass accessHelper) throws NotFoundException, CannotCompileException;

    ClassPool getPool();
}
