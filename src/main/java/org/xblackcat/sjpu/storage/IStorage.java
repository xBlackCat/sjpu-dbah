package org.xblackcat.sjpu.storage;

/**
 * 12.02.13 15:47
 *
 * @author xBlackCat
 */
public interface IStorage extends IAHFactory {
    /**
     * Starts a transaction with default transaction isolation level. See {@linkplain ITx} interface description for details
     *
     * @return an IBatch object associated with transaction
     * @throws StorageException in case fail to start transaction
     */
    ITx beginTransaction() throws StorageException;

    /**
     * Starts a transaction with specified transaction isolation level. See {@linkplain ITx} interface description for details
     *
     * @param transactionIsolationLevel desired transaction isolation level. Should be supported by driver implementation.
     * @return an IBatch object associated with transaction
     * @throws StorageException in case fail to start transaction
     */
    ITx beginTransaction(int transactionIsolationLevel) throws StorageException;

    void shutdown() throws StorageException;
}
