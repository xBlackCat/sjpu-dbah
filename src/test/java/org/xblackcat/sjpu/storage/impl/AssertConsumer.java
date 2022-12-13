package org.xblackcat.sjpu.storage.impl;

import org.junit.jupiter.api.Assertions;
import org.xblackcat.sjpu.storage.ConsumeException;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;

/**
 * 15.02.14 15:17
 *
 * @author xBlackCat
 */
public class AssertConsumer<T> implements IRowConsumer<T> {
    private final T expected;
    private boolean done = false;

    public AssertConsumer(T expected) {
        this.expected = expected;
    }

    @Override
    public boolean consume(T o) throws ConsumeException {
        if (done) {
            Assertions.fail("Double invocation.");
        }
        Assertions.assertEquals(expected, o);
        done = true;
        return false;
    }
}
