package org.xblackcat.sjpu.storage.consumer;

import org.xblackcat.sjpu.storage.ConsumeException;

/**
 * Process (consume) an object after conversion from result row. If object can't be consumed a
 * {@linkplain org.xblackcat.sjpu.storage.ConsumeException ConsumeException} should be thrown.
 *
 * @author xBlackCat
 */
public interface IRowConsumer<O> {
    /**
     * Consume an object mapped from query result set row.
     *
     * @param o object to consume
     * @return true to stop consuming rows
     * @throws ConsumeException if passed object can't be consumed
     */
    boolean consume(O o) throws ConsumeException;
}
