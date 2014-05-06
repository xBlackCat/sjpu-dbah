package org.xblackcat.sjpu.storage.consumer;

import java.util.EnumSet;
import java.util.Set;

/**
 * 15.02.14 10:16
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
