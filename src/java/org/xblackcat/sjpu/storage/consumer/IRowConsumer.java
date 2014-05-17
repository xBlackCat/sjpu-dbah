package org.xblackcat.sjpu.storage.consumer;

import org.xblackcat.sjpu.storage.ConsumeException;

/**
 * 15.02.14 8:53
 *
 * @author xBlackCat
 */
public interface IRowConsumer<O> {
    /**
     * Consume an object mapped from query result set row.
     *
     * @param o object to consume
     * @return true to stop consuming rows
     */
    boolean consume(O o) throws ConsumeException;
}
