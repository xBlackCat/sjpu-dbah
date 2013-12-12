package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ASUS
 */

final class SingleConnectionQueryHelper extends AQueryHelper {
    private final Connection con;

    public SingleConnectionQueryHelper(AQueryHelper parentHelper) throws SQLException {
        this.con = parentHelper.getConnection();
        mappers.putAll(parentHelper.mappers);
    }

    @Override
    public <T> List<T> execute(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        try {
            try (PreparedStatement st = QueryHelperUtils.constructSql(con, sql, Statement.NO_GENERATED_KEYS, parameters)) {
                try (ResultSet rs = st.executeQuery()) {
                    List<T> res = new ArrayList<>();
                    while (rs.next()) {
                        res.add(c.convert(rs));
                    }

                    return Collections.unmodifiableList(res);
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
            try (PreparedStatement st = QueryHelperUtils.constructSql(con, sql, Statement.NO_GENERATED_KEYS, parameters)) {
                return st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e);
        }
    }


    @Override
    public <T> T insert(IToObjectConverter<T> c, String sql, Object... parameters) throws StorageException {
        try {
            try (PreparedStatement st = QueryHelperUtils.constructSql(
                    con,
                    sql,
                    c == null ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS,
                    preProcessing(parameters)
            )) {
                st.executeUpdate();
                if (c != null) {
                    try (ResultSet keys = st.getGeneratedKeys()) {
                        if (keys.next()) {
                            return c.convert(keys);
                        }
                    }
                }

                return null;
            }
        } catch (SQLException e) {
            throw new StorageException("Can not execute query " + QueryHelperUtils.constructDebugSQL(sql, parameters), e);
        }
    }

    @Override
    public Connection getConnection() {
        return con;
    }
}
