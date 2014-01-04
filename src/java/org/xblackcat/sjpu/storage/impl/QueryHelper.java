package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ASUS
 */

public final class QueryHelper implements IQueryHelper {
    private final IConnectionFactory connectionFactory;

    public QueryHelper(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> List<T> execute(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        try {
            try (Connection con = connectionFactory.getConnection()) {
                try (PreparedStatement st = con.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
                    QueryHelperUtils.fillStatement(st, parameters);
                    try (ResultSet rs = st.executeQuery()) {
                        List<T> res = new ArrayList<>();
                        while (rs.next()) {
                            res.add(c.convert(rs));
                        }

                        return Collections.unmodifiableList(res);
                    }
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e);
        }
    }

    @Override
    public <T> T executeSingle(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        Collection<T> col = execute(c, sql, parameters);
        if (col.size() > 1) {
            throw new StorageException("Expected one or zero results on query " + QueryHelperUtils.constructDebugSQL(sql, parameters));
        }
        if (col.isEmpty()) {
            return null;
        } else {
            return col.iterator().next();
        }
    }

    @Override
    public int update(String sql, Object... parameters) throws StorageException {
        try {
            try (Connection con = connectionFactory.getConnection()) {
                try (PreparedStatement st = con.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
                    QueryHelperUtils.fillStatement(st, parameters);

                    return st.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e);
        }
    }

    @Override
    public <T> T insert(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        try {
            try (Connection con = connectionFactory.getConnection()) {
                int keys = c == null ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS;

                try (PreparedStatement st = con.prepareStatement(sql, keys)) {
                    QueryHelperUtils.fillStatement(st, parameters);
                    st.executeUpdate();
                    if (c != null) {
                        try (ResultSet genKeys = st.getGeneratedKeys()) {
                            if (genKeys.next()) {
                                return c.convert(genKeys);
                            }
                        }
                    }

                    return null;
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionFactory.getConnection();
    }

    @Override
    public void shutdown() throws StorageException {
        connectionFactory.shutdown();
    }
}
