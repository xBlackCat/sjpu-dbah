package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author ASUS
 */

final class QueryHelper implements IQueryHelper {
    private final IConnectionFactory connectionFactory;

    public QueryHelper(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> List<T> execute(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        try {
            try (Connection con = connectionFactory.getReadConnection()) {
                try (PreparedStatement st = constructSql(con, sql, Statement.NO_GENERATED_KEYS, parameters)) {
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
            throw new StorageException("Can not execute query " + constructDebugSQL(sql, parameters), e);
        }
    }

    @Override
    public <T> T executeSingle(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        Collection<T> col = execute(c, sql, parameters);
        if (col.size() > 1) {
            throw new StorageException("Expected one or zero results on query " + constructDebugSQL(sql, parameters));
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
            try (Connection con = connectionFactory.getWriteConnection()) {
                try (PreparedStatement st = constructSql(con, sql, Statement.NO_GENERATED_KEYS, parameters)) {
                    return st.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + constructDebugSQL(sql, parameters), e);
        }
    }

    @Override
    public IConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    private static PreparedStatement constructSql(
            Connection con,
            String sql,
            int keys,
            Object... parameters
    ) throws SQLException {
        @SuppressWarnings("MagicConstant") PreparedStatement pstmt = con.prepareStatement(sql, keys);
        fillStatement(pstmt, parameters);

        return pstmt;
    }

    private static void fillStatement(PreparedStatement pstmt, Object... parameters) throws SQLException {
        // Fill parameters if any
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof Boolean) {
                    pstmt.setInt(i + 1, ((Boolean) (parameters[i])) ? 1 : 0);
                } else {
                    pstmt.setObject(i + 1, parameters[i]);
                }
            }
        }
    }

    private static String constructDebugSQL(String sql, Object... parameters) {
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
