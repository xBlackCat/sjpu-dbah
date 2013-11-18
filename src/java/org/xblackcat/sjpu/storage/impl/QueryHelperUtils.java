package org.xblackcat.sjpu.storage.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;

/**
 * 18.11.13 12:30
 *
 * @author xBlackCat
 */
public class QueryHelperUtils {
    protected static PreparedStatement constructSql(
            Connection con,
            String sql,
            int keys,
            Object... parameters
    ) throws SQLException {
        @SuppressWarnings("MagicConstant") PreparedStatement pstmt = con.prepareStatement(sql, keys);
        fillStatement(pstmt, parameters);

        return pstmt;
    }

    protected static void fillStatement(PreparedStatement pstmt, Object... parameters) throws SQLException {
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

    protected static String constructDebugSQL(String sql, Object... parameters) {
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
