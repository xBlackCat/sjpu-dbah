package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.ConsumeException;
import org.xblackcat.sjpu.storage.IQueryHelper;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.*;

/**
 * @author ASUS
 */

public final class QueryHelper implements IQueryHelper {
    private final IConnectionFactory connectionFactory;

    public QueryHelper(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> void execute(IRowConsumer<T> consumer, IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        try {
            try (Connection con = connectionFactory.getConnection()) {
                try (PreparedStatement st = con.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
                    QueryHelperUtils.fillStatement(st, parameters);

                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            T rowObject = c.convert(rs);

                            if (consumer.consume(rowObject)) {
                                break;
                            }
                        }
                    } catch (ConsumeException | RuntimeException e) {
                        throw new StorageException(
                                "Can not consume result for query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e);
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
    public int update(String ddl) throws StorageException {
        try {
            try (Connection con = connectionFactory.getConnection()) {
                try (Statement st = con.createStatement()) {
                    return st.executeUpdate(ddl);
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + QueryHelperUtils.constructDebugSQL(ddl), e);
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
