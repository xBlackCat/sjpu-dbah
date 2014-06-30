package org.xblackcat.sjpu.storage.consumer;

import java.util.HashSet;
import java.util.Set;

/**
 * Consume all objects into {@linkplain java.util.Set Set}. {@linkplain java.util.HashSet HashSet} is used as default container
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
