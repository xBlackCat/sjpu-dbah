package org.xblackcat.sjpu.storage;

/**
 * 12.02.13 15:47
 *
 * @author xBlackCat
 */
public interface IStorage extends IAHFactory {
    /**
     * Starts a transaction. See {@linkplain ITxAH} interface description for details
     *
     * @return an IBatch object associated with transaction
     */
    ITxAH beginTransaction() throws StorageException;

    /**
     * Starts a transaction. See {@linkplain ITxAH} interface description for details
     *
     * @return an IBatch object associated with transaction
     * @param transactionIsolationLevel
     */
    ITxAH beginTransaction(int transactionIsolationLevel) throws StorageException;

    void shutdown() throws StorageException;
}
