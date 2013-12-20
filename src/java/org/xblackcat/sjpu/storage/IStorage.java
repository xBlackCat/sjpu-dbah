package org.xblackcat.sjpu.storage;

/**
 * 12.02.13 15:47
 *
 * @author xBlackCat
 */
public interface IStorage extends IAHFactory {
    /**
     * Starts a transaction. See {@linkplain IBatch} interface description for details
     *
     * @return an IBatch object associated with transaction
     */
    IBatch openTransaction() throws StorageException;

    /**
     * Starts a transaction. See {@linkplain org.xblackcat.sjpu.storage.IBatch} interface description for details
     *
     * @return an IBatch object associated with transaction
     * @param transactionIsolationLevel
     */
    IBatch openTransaction(int transactionIsolationLevel) throws StorageException;

    void shutdown() throws StorageException;
}
