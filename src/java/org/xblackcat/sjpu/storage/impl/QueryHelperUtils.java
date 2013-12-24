package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.converter.StandardMappers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;

/**
 * 18.11.13 12:30
 *
 * @author xBlackCat
 */
public class QueryHelperUtils {
    protected static void fillStatement(PreparedStatement pstmt, Object... parameters) throws SQLException {
        // Fill parameters if any
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) (parameters[i]));
                } else if (parameters[i] instanceof Date) {
                    pstmt.setTimestamp(i + 1, StandardMappers.dateToTimestamp((Date) parameters[i]));
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
