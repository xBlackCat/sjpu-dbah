package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.ITx;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.TxSingleConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.skel.Definer;
import org.xblackcat.sjpu.storage.skel.IBuilder;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.sql.SQLException;
import java.util.Map;

/**
 * 15.11.13 14:06
 *
 * @author xBlackCat
 */
class TxFactory extends AnAHFactory implements ITx {
    private static final Log log = LogFactory.getLog(TxFactory.class);

    private boolean rollbackOnClose = true;
    private boolean transactionDone = false;

    TxFactory(
            IConnectionFactory connectionFactory,
            int transactionIsolationLevel,
            Definer<IAH, IConnectionFactory> definer,
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            IBuilder<IAH, IConnectionFactory> methodBuilder
    ) throws SQLException {
        super(definer, new TxSingleConnectionFactory(connectionFactory, transactionIsolationLevel), typeMapper, rowSetConsumers, methodBuilder);
    }

    @Override
    public void commit() throws StorageException {
        if (isTransactionDone()) {
            throw new StorageException("Transaction is already done");
        }

        setTransactionDone();

        try {
            factory.getConnection().commit();
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
            factory.getConnection().rollback();
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

        factory.shutdown();
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
