package org.xblackcat.sjpu.storage;

/**
 * An instance of the interface holds context of open transactions. All AccessHelpers obtained from the context will be belong to the
 * transaction only. Invoke {@linkplain #commit()} method before closing the transaction to fix performed changes. If neither
 * {@linkplain #commit()} nor {@linkplain #rollback()} is invoked before {@linkplain #close()} the transaction all changes will be
 * reverted automatically on closing. After It is not allowed to obtain new AccessHelper objects after {@linkplain #commit()}
 * or {@linkplain #rollback()}.
 * <p/>
 * <p/>
 * <code><pre>
 * try (IBatch tx = s.openTransaction()) {
 *   ISimpleAH ah = tx.get(ISimpleAH.class);
 * <p/>
 *   // do action
 * <p/>
 *   tx.commit();
 * }
 * </pre></code>
 * 15.11.13 14:05
 *
 * @author xBlackCat
 */
public interface ITxAH extends IAHFactory, AutoCloseable {
    void commit() throws StorageException;

    void rollback() throws StorageException;

    @Override
    public void close() throws StorageException;
}
