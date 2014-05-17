package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.connection.IDBConfig;
import org.xblackcat.sjpu.storage.connection.SimplePooledConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.ToEnumSetConsumer;
import org.xblackcat.sjpu.storage.consumer.ToListConsumer;
import org.xblackcat.sjpu.storage.consumer.ToSetConsumer;
import org.xblackcat.sjpu.storage.impl.QueryHelper;

import java.util.*;

/**
 * 15.11.13 14:23
 *
 * @author xBlackCat
 */
public class StorageUtils {
    public static final Map<Class<?>, Class<? extends IRowSetConsumer>> DEFAULT_ROWSET_CONSUMERS;

    static {
        Map<Class<?>, Class<? extends IRowSetConsumer>> map = new HashMap<>();
        map.put(List.class, ToListConsumer.class);
        map.put(Set.class, ToSetConsumer.class);
        map.put(EnumSet.class, ToEnumSetConsumer.class);

        DEFAULT_ROWSET_CONSUMERS = Collections.unmodifiableMap(map);
    }


    public static IQueryHelper buildQueryHelper(IDBConfig settings) {
        try {
            return new QueryHelper(new SimplePooledConnectionFactory(settings));
        } catch (StorageException e) {
            throw new RuntimeException("Can not initialize DB connection factory", e);
        }
    }
}
