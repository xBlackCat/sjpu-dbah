package org.xblackcat.sjpu.storage.consumer;

import java.util.ArrayList;
import java.util.List;

/**
 * 15.02.14 10:16
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
