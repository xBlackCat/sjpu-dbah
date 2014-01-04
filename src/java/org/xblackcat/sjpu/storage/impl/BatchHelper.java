package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.IBatch;
import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 15.11.13 14:06
 *
 * @author xBlackCat
 */
class BatchHelper extends AnAHFactory implements IBatch {
    private static final Log log = LogFactory.getLog(BatchHelper.class);

    private boolean rollbackOnClose = true;
    private boolean transactionDone = false;

    BatchHelper(IQueryHelper helper, int transactionIsolationLevel, TypeMapper typeMapper) throws SQLException {
        super(new SingleConnectionQueryHelper(helper), typeMapper);
        final Connection con = queryHelper.getConnection();
        con.setAutoCommit(false);
        if (transactionIsolationLevel != -1) {
            con.setTransactionIsolation(transactionIsolationLevel);
        }
    }

    @Override
    public void commit() throws StorageException {
        if (isTransactionDone()) {
            throw new StorageException("Transaction is already done");
        }

        setTransactionDone();

        try {
            queryHelper.getConnection().commit();
        } catch (SQLException e) {
            throw new StorageException("Can't commit changes to database", e);
        }
    }

    @Override
    public void rollback() throws StorageException {
        if (isTransactionDone()) {
            throw new StorageException("Transaction is already done");
        }

        setTransactionDone();

        try {
            queryHelper.getConnection().rollback();
        } catch (SQLException e) {
            throw new StorageException("Can't rollback changes", e);
        }
    }

    @Override
    public void close() throws StorageException {
        if (rollbackOnClose()) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("Perform automatic rollback for non-committed transaction");
                }

                rollback();
            } catch (Exception e) {
                log.error("An exception occurs while automatic rollback is performed", e);
            }
        }

        setTransactionDone();

        try {
            queryHelper.getConnection().close();
        } catch (SQLException e) {
            throw new StorageException("Can't close database connection", e);
        }
    }

    protected boolean rollbackOnClose() {
        lock.readLock().lock();
        try {
            return this.rollbackOnClose;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected boolean isTransactionDone() {
        lock.readLock().lock();
        try {
            return this.transactionDone;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void setTransactionDone() {
        lock.writeLock().lock();
        try {
            transactionDone = true;
            rollbackOnClose = false;
            helpers.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
