package org.xblackcat.sjpu.storage.connection;

import java.sql.SQLException;

/**
 * 17.05.2014 18:06
 *
 * @author xBlackCat
 */
public class TxSingleConnectionFactory extends SingleConnectionFactory {
    public TxSingleConnectionFactory(IConnectionFactory factory, int transactionIsolationLevel) throws SQLException {
        super(factory);
        con.setAutoCommit(false);
        if (transactionIsolationLevel != -1) {
            con.setTransactionIsolation(transactionIsolationLevel);
        }
    }
}
