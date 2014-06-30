package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.IDBConfig;
import org.xblackcat.sjpu.storage.connection.SimplePooledConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.ToEnumSetConsumer;
import org.xblackcat.sjpu.storage.consumer.ToListConsumer;
import org.xblackcat.sjpu.storage.consumer.ToSetConsumer;

import java.util.*;
import java.util.regex.Matcher;

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

    public static IConnectionFactory buildConnectionFactory(IDBConfig settings) throws GeneratorException {
        try {
            return new SimplePooledConnectionFactory(settings);
        } catch (StorageException e) {
            throw new GeneratorException("Can not initialize DB connection factory", e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String constructDebugSQL(String sql, Object... parameters) {
        String query = sql;

        for (Object value : parameters) {
            String str;
            if (value == null) {
                str = "NULL";
            } else if (value instanceof String) {
                str = "'" + Matcher.quoteReplacement(value.toString()) + "'";
            } else {
                str = Matcher.quoteReplacement(value.toString());
            }
            query = query.replaceFirst("\\?", str);
        }

        return query;
    }
}
