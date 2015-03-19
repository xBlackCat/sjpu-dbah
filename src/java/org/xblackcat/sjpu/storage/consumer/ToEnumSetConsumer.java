package org.xblackcat.sjpu.storage.consumer;

import java.util.EnumSet;
import java.util.Set;

/**
 * Consume all objects into {@linkplain java.util.EnumSet EnumSet}. Input objects should be enum constants of the same Enum object
 *
 * @author xBlackCat
 */
public class ToEnumSetConsumer<E extends Enum<E>> implements IRowSetConsumer<Set<E>, E> {
    private final EnumSet<E> set;

    public ToEnumSetConsumer(Class<E> clazz) {
        set = EnumSet.noneOf(clazz);
    }

    @Override
    public boolean consume(E o) {
        set.add(o);
        return false;
    }

    public EnumSet<E> getRowsHolder() {
        return set;
    }
}
