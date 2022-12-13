package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.ann.CloseResources;

/**
 * Marker interface to build statement-cached Access Helper. For each instance of the access helper an connection is
 * borrowed until {@linkplain #close()} method is invoked.
 *
 * @author xBlackCat
 */

public interface IBatchedAH extends IAH, AutoCloseable {
    /**
     * Close associated connection and other resources
     */
    @Override
    @CloseResources
    void close() throws StorageException;
}
