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
class InterfaceAHBuilder extends AnAbstractAHBuilder {
    protected <T extends IAH> CtClass defineCtClass(Class<T> target) throws NotFoundException, CannotCompileException {
        CtClass baseClass = pool.get(target.getName());
        final CtClass accessHelper = baseClass.makeNestedClass("Impl", true);
        accessHelper.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        accessHelper.addInterface(pool.get(target.getName()));
        accessHelper.setSuperclass(pool.get(AnAH.class.getName()));

        return accessHelper;
    }
}
