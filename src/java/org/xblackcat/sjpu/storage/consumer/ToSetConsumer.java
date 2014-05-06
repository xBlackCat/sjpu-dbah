package org.xblackcat.sjpu.storage.consumer;

import java.util.HashSet;
import java.util.Set;

/**
 * 15.02.14 10:16
 *
 * @author xBlackCat
 */
public class ToSetConsumer<T> implements IRowSetConsumer<Set<T>, T> {
    private final Set<T> set = new HashSet<>();

    @Override
    public boolean consume(T o) {
        set.add(o);
        return false;
    }

    public Set<T> getRowsHolder() {
        return set;
    }
}
