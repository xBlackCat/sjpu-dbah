package org.xblackcat.sjpu.storage.consumer;

import org.xblackcat.sjpu.storage.ConsumeException;

/**
 * 15.02.14 10:21
 *
 * @author xBlackCat
 */
public class SingletonConsumer<T> implements IRowSetConsumer<T, T> {
    private T obj;
    private boolean set = false;

    @Override
    public boolean consume(T o) throws ConsumeException {
        if (set) {
            throw new ConsumeException("Expected one or zero results on query ");
        }

        obj = o;
        set = true;

        return false;
    }

    public T getRowsHolder() {
        return obj;
    }
}
