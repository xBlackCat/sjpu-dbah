package org.xblackcat.sjpu.storage.impl;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 11.03.13 13:16
 *
 * @author xBlackCat
 */
interface IMethodBuilder<T extends Annotation> {
    void buildMethod(
            CtClass accessHelper,
            Method m,
            T annotation
    ) throws NotFoundException, ReflectiveOperationException, CannotCompileException;
}
