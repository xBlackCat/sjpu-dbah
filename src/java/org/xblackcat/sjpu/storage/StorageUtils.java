package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.connection.IDatabaseSettings;
import org.xblackcat.sjpu.storage.connection.SimplePooledConnectionFactory;
import org.xblackcat.sjpu.storage.impl.IQueryHelper;
import org.xblackcat.sjpu.storage.impl.QueryHelper;

/**
 * 15.11.13 14:23
 *
 * @author xBlackCat
 */
public class StorageUtils {
    public static IQueryHelper buildQueryHelper(IDatabaseSettings settings) {
        try {
            return new QueryHelper(new SimplePooledConnectionFactory(settings));
        } catch (StorageException e) {
            throw new RuntimeException("Can not initialize DB connection factory", e);
        }
    }
}
