package org.xblackcat.sjpu.storage.consumer;

import java.util.HashSet;
import java.util.Set;

/**
 * 15.02.14 10:16
 *
 * @author xBlackCat
 */
public class ToSetConsumer<T> implements IRowSetConsumer<Set<T>, T> {
    private final Set<T> list = new HashSet<>();

    @Override
    public boolean consume(T o) {
        list.add(o);
        return false;
    }

    public Set<T> getRowsHolder() {
        return list;
    }
}
