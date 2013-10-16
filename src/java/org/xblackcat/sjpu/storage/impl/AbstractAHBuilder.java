package org.xblackcat.sjpu.storage.impl;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import org.xblackcat.sjpu.storage.IAH;

/**
 * 21.02.13 12:01
 *
 * @author xBlackCat
 */
class AbstractAHBuilder extends AnAbstractAHBuilder {
    protected <T extends IAH> CtClass defineCtClass(Class<T> target) throws NotFoundException, CannotCompileException {
        CtClass thisClass = pool.get(target.getName());
        CtClass accessHelper = thisClass.makeNestedClass("Impl", true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.setSuperclass(pool.get(target.getName()));
        return accessHelper;
    }
}
