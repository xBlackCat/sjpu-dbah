package org.xblackcat.sjpu.storage;

/**
 * 12.02.13 15:47
 *
 * @author xBlackCat
 */
public interface IStorage extends IAHFactory {
    /**
     * Starts a transaction. See {@linkplain ITx} interface description for details
     *
     * @return an IBatch object associated with transaction
     */
    ITx beginTransaction() throws StorageException;

    /**
     * Starts a transaction. See {@linkplain ITx} interface description for details
     *
     * @return an IBatch object associated with transaction
     * @param transactionIsolationLevel
     */
    ITx beginTransaction(int transactionIsolationLevel) throws StorageException;

    void shutdown() throws StorageException;
}
