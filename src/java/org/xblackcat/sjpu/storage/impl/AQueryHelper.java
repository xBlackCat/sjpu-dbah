package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.AnObjectMapper;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 21.11.13 17:50
 *
 * @author xBlackCat
 */
public abstract class AQueryHelper {
    protected final Map<Class, AnObjectMapper> mappers = new HashMap<>();

    @SafeVarargs
    protected AQueryHelper(Class<? extends AnObjectMapper>... mappers) {
        try {
            for (Class<? extends AnObjectMapper> om : mappers) {
                final AnObjectMapper mapper;
                mapper = om.newInstance();

                this.mappers.put(mapper.getOrigClass(), mapper);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new StorageSetupException("Mapper should have default constructor", e);
        }

    }

    @SuppressWarnings("unchecked")
    protected Object[] preProcessing(Object[] params) {
        final Object[] processed = new Object[params.length];

        int i = 0;
        while (i < params.length) {
            Object o = params[i];

            AnObjectMapper m = mappers.get(o.getClass());
            if (m == null) {
                processed[i] = o;
            } else {
                processed[i] = m.convert(o);
            }

            i++;
        }

        return processed;
    }

    public abstract <T> List<T> execute(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    public abstract <T> T executeSingle(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException;

    public abstract int update(String sql, Object... parameters) throws StorageException;

    public abstract Connection getConnection() throws SQLException;
}
