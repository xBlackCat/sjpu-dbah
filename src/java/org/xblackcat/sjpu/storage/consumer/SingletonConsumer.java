package org.xblackcat.sjpu.storage.consumer;

import org.xblackcat.sjpu.storage.ConsumeException;

/**
 * Default consumer for reading single row. If result set has more than
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
