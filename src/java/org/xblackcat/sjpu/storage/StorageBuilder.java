package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.IDBConfig;
import org.xblackcat.sjpu.storage.connection.SimplePooledConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.impl.Storage;
import org.xblackcat.sjpu.storage.typemap.DateMapper;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;
import org.xblackcat.sjpu.storage.typemap.IMapFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 16.02.14 16:32
 *
 * @author xBlackCat
 */
public class StorageBuilder {
    public static IStorage defaultStorage(IDBConfig settings) throws StorageException {
        IConnectionFactory factory = new SimplePooledConnectionFactory(settings);

        StorageBuilder builder = new StorageBuilder();
        builder.setConnectionFactory(factory);
        return builder.build();
    }

    private IConnectionFactory connectionFactory = null;
    private final Map<Class<?>, Class<? extends IRowSetConsumer>> consumers = new HashMap<>();
    private final List<IMapFactory<?, ?>> mappers = new ArrayList<>();

    public StorageBuilder() {
        this(true, true);
    }

    public StorageBuilder(boolean defaultConsumers, boolean defaultMappers) {
        if (defaultConsumers) {
            consumers.putAll(StorageUtils.DEFAULT_ROWSET_CONSUMERS);
        }
        if (defaultMappers) {
            mappers.add(new EnumToStringMapper());
            mappers.add(new DateMapper());
        }
    }

    public StorageBuilder setConnectionFactory(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    public StorageBuilder addRowSetConsumer(Class<?> returnClass, Class<? extends IRowSetConsumer> consumerClass) {
        consumers.put(returnClass, consumerClass);
        return this;
    }

    public StorageBuilder addMapper(IMapFactory<?, ?> typeMapper) {
        mappers.add(typeMapper);
        return this;
    }

    public IStorage build() {
        if (connectionFactory == null) {
            throw new StorageSetupException("Connection factory should be specified.");
        }

        IMapFactory<?, ?>[] mappers = this.mappers.toArray(new IMapFactory<?, ?>[this.mappers.size()]);

        return new Storage(connectionFactory, consumers, mappers);

    }
}
