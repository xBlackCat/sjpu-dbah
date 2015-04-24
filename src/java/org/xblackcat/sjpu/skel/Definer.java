package org.xblackcat.sjpu.skel;

import javassist.*;

/**
 * 15.11.13 15:37
 *
 * @author xBlackCat
 */
public class Definer<Base> implements IDefiner<Base> {
    private final Class<? extends Base> baseClass;
    private final Class<?>[] paramClasses;
    private final ClassPool pool;

    public Definer(ClassPool pool, Class<? extends Base> baseClass, Class<?>... paramClasses) {
        this.baseClass = baseClass;
        this.pool = pool;
        this.paramClasses = paramClasses;
    }

    @Override
    public CtClass getBaseCtClass() throws NotFoundException {
        return pool.get(baseClass.getName());
    }

    @Override
    public boolean isAssignable(Class<?> clazz) {
        return baseClass.isAssignableFrom(clazz);
    }

    @Override
    public String getNestedClassName() {
        return baseClass.getSimpleName() + "Impl";
    }

    @Override
    public CtConstructor buildCtConstructor(CtClass accessHelper) throws NotFoundException, CannotCompileException {
        return CtNewConstructor.make(
                BuilderUtils.toCtClasses(pool, paramClasses),
                BuilderUtils.EMPTY_LIST,
                "{ super($$); }",
                accessHelper
        );
    }

    @Override
    public ClassPool getPool() {
        return pool;
    }
}
