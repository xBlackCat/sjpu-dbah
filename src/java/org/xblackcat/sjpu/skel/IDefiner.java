package org.xblackcat.sjpu.skel;

import javassist.*;

/**
 * 24.04.2015 12:34
 *
 * @author xBlackCat
 */
public interface IDefiner<Base> {
    CtClass getBaseCtClass() throws NotFoundException;

    boolean isAssignable(Class<?> clazz);

    String getNestedClassName();

    CtConstructor buildCtConstructor(CtClass accessHelper) throws NotFoundException, CannotCompileException;

    ClassPool getPool();
}
