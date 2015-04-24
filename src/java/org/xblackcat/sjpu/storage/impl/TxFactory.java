package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.skel.IBuilder;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.IFunctionalAH;
import org.xblackcat.sjpu.storage.ITx;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.TxSingleConnectionFactory;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

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
            IBuilder<IAH, IConnectionFactory> methodBuilder,
            IBuilder<IFunctionalAH, IConnectionFactory> functionalBuilder
    ) throws SQLException {
        super(new TxSingleConnectionFactory(connectionFactory, transactionIsolationLevel), typeMapper, methodBuilder, functionalBuilder);
    }

    @Override
    public void commit() throws StorageException {
        if (transactionDone.get()) {
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
        if (transactionDone.get()) {
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

        factory.shutdown();
    }

    protected void setTransactionDone() {
        lock.writeLock().lock();
        try {
            transactionDone.set(true);
            rollbackOnClose.set(false);
            helpers.clear();
        } finally {
            lock.writeLock().unlock();
        }

    }
}
