package org.xblackcat.sjpu.skel;

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
public interface IMethodBuilder<T extends Annotation> {
    void buildMethod(CtClass accessHelper, Method m) throws NotFoundException, ReflectiveOperationException, CannotCompileException;

    Class<T> getAnnotationClass();
}
