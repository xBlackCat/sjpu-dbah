package org.xblackcat.sjpu.storage.consumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Consume all objects into {@linkplain java.util.List List}. {@linkplain java.util.ArrayList ArrayList} is used as default container
 *
 * @author xBlackCat
 */
public class ToListConsumer<T> implements IRowSetConsumer<List<T>, T> {
    private final List<T> list = new ArrayList<>();

    @Override
    public boolean consume(T o) {
        list.add(o);
        return false;
    }

    public List<T> getRowsHolder() {
        return list;
    }
}
