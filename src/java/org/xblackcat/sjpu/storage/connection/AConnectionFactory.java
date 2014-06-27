package org.xblackcat.sjpu.storage.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * @author xBlackCat
 */

public abstract class AConnectionFactory implements IConnectionFactory {
    protected final Log log;
    protected final IDBConfig settings;

    AConnectionFactory(IDBConfig settings) throws StorageException {
        log = LogFactory.getLog(getClass());
        this.settings = settings;

        try {
            Class.forName(this.settings.getDriver()).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new StorageException("Can not initialize JDBC driver.", e);
        }
    }
}
