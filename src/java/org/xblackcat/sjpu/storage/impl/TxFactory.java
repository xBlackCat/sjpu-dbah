package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.builder.IBuilder;
import org.xblackcat.sjpu.builder.IFactory;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.TxSingleConnectionFactory;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 15.11.13 14:06
 *
 * @author xBlackCat
 */
class TxFactory extends AnAHFactory implements ITx {
    private static final Log log = LogFactory.getLog(TxFactory.class);

    private AtomicBoolean rollbackOnClose = new AtomicBoolean(true);
    private AtomicBoolean transactionDone = new AtomicBoolean(false);

    TxFactory(
            IConnectionFactory connectionFactory,
            int transactionIsolationLevel,
            TypeMapper typeMapper,
            IBuilder<IAH> methodBuilder,
            IBuilder<IFunctionalAH> functionalBuilder,
            IFactory<IBatchedAH> batchedFactory
    ) throws SQLException {
        super(
                new TxSingleConnectionFactory(connectionFactory, transactionIsolationLevel),
                typeMapper,
                methodBuilder,
                functionalBuilder,
                batchedFactory
        );
    }

    @Override
    public void commit() throws StorageException {
        if (transactionDone.get()) {
            throw new StorageException("Transaction is already done");
        }

        setTransactionDone();

        try {
            connectionFactory.getConnection().commit();
        } catch (SQLException e) {
            throw new StorageException("Can't commit changes to database", e);
        }
    }

    @Override
    public void rollback() throws StorageException {
        if (transactionDone.get()) {
            throw new StorageException("Transaction is already done");
        }

        setTransactionDone();

        try {
            connectionFactory.getConnection().rollback();
        } catch (SQLException e) {
            throw new StorageException("Can't rollback changes", e);
        }
    }

    @Override
    public void close() throws StorageException {
        if (rollbackOnClose.get()) {
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

        connectionFactory.shutdown();
    }

    protected void setTransactionDone() {
        ReadWriteLock commonFactoryLock = commonFactory.getLock();
        ReadWriteLock functionalFactoryLock = functionalFactory.getLock();
        commonFactoryLock.writeLock().lock();
        try {
            functionalFactoryLock.writeLock().lock();
            try {
                transactionDone.set(true);
                rollbackOnClose.set(false);

                commonFactory.purge();
                functionalFactory.purge();
                batchedFactory.purge();
            } finally {
                functionalFactoryLock.writeLock().unlock();
            }
        } finally {
            commonFactoryLock.writeLock().unlock();
        }

    }
}
