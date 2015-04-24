package org.xblackcat.sjpu.skel;

import org.xblackcat.sjpu.skel.*;
import org.xblackcat.sjpu.storage.IFunctionalAH;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Functional builder allows only one abstract method per class/interface.
 *
 * @author xBlackCat
 */
public class FunctionalMethodBuilder<Base, Helper> extends MethodBuilder<Base, Helper> {
    public FunctionalMethodBuilder(Definer<Base, Helper> definerF, IMethodBuilder<?>... builders) {
        super(definerF, builders);
    }

    @Override
    public <T extends Base> T build(
            Class<T> target,
            Helper cf
    ) throws GeneratorException {
        int abstractCount = 0;
        for (Method m : target.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                abstractCount++;
            }
        }
        for (Method m : target.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers())) {
                abstractCount++;
            }
        }

        if (abstractCount != 1) {
            throw new GeneratorException(
                    "Only single abstract method is allowed for implementation of " +
                            BuilderUtils.getName(IFunctionalAH.class) + ". " + BuilderUtils.getName(target) + " has " +
                            abstractCount
            );
        }

        return super.build(target, cf);
    }
}
