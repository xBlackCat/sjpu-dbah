package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.IDatabaseSettings;
import org.xblackcat.sjpu.storage.connection.SimplePooledConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.impl.QueryHelper;
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
    public static IStorage defaultStorage(IDatabaseSettings settings) throws StorageException {
        IConnectionFactory factory = new SimplePooledConnectionFactory(settings);

        StorageBuilder builder = new StorageBuilder();
        builder.setConnectionFactory(factory);
        return builder.build();
    }

    private IConnectionFactory connectionFactory = null;
    private IQueryHelper queryHelper = null;
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

    public void setConnectionFactory(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setQueryHelper(IQueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    public void addRowSetConsumer(Class<?> returnClass, Class<? extends IRowSetConsumer> consumerClass) {
        consumers.put(returnClass, consumerClass);
    }

    public void addMapper(IMapFactory<?, ?> typeMapper) {
        mappers.add(typeMapper);
    }

    public IStorage build() {
        if (queryHelper == null) {
            if (connectionFactory == null) {
                throw new StorageSetupException("Connection factory or query helper should be specified.");
            }

            queryHelper = new QueryHelper(connectionFactory);
        }

        IMapFactory<?, ?>[] mappers = this.mappers.toArray(new IMapFactory<?, ?>[this.mappers.size()]);

        return new Storage(queryHelper, consumers, mappers);

    }
}
