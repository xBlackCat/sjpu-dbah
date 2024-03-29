package org.xblackcat.sjpu.storage.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * @author xBlackCat
 */

public abstract class AConnectionFactory implements IConnectionFactory {
    protected final Log log;

    AConnectionFactory(String driver) throws StorageException {
        log = LogFactory.getLog(getClass());

        try {
            Class.forName(driver).getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new StorageException("Can not initialize JDBC driver.", e);
        }
    }
}
