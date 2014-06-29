package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 14.06.2014 23:37
 *
 * @author xBlackCat
 */
class SqlStringUtils {
    private final static Pattern SQL_PART_IDX = Pattern.compile("\\{(\\d+)\\}");

    public static List<Integer> appendSqlWithParts(StringBuilder body, String sql, Map<Integer, ConverterInfo.SqlArg> sqlParts) {
        if (sqlParts.isEmpty()) {
            body.append("java.lang.String sql = \"");
            body.append(StringEscapeUtils.escapeJava(sql));
            body.append("\";\n");
            return Collections.emptyList();
        } else {
            boolean hasOptionalParts = false;
            for (ConverterInfo.SqlArg arg : sqlParts.values()) {
                if (arg.sqlPart != null) {
                    hasOptionalParts = true;
                    break;
                }
            }

            if (hasOptionalParts) {
                return buildStringBuilder(body, sql, sqlParts);
            } else {
                buildConcatenation(body, sql, sqlParts);
                return Collections.emptyList();
            }
        }
    }

    private static List<Integer> buildStringBuilder(StringBuilder body, String sql, Map<Integer, ConverterInfo.SqlArg> sqlParts) {
        List<Integer> optionalIndexes = new ArrayList<>();

        Matcher m = SQL_PART_IDX.matcher(sql);
        int startPos = 0;
        while (m.find()) {
            final Integer idx = Integer.valueOf(m.group(1));
            ConverterInfo.SqlArg argRef = sqlParts.get(idx);

            if (argRef != null) {
                body.append("\"");
                body.append(StringEscapeUtils.escapeJava(sql.substring(startPos, m.start())));
                body.append("\" + $");
                body.append(argRef.argIdx + 1);
                body.append(" + ");

                startPos = m.end();
            }
        }

        body.append("\"");
        body.append(StringEscapeUtils.escapeJava(sql.substring(startPos)));
        body.append("\"");
        return null;
    }

    protected static void buildConcatenation(StringBuilder body, String sql, Map<Integer, ConverterInfo.SqlArg> sqlParts) {
        Matcher m = SQL_PART_IDX.matcher(sql);
        int startPos = 0;
        body.append("java.lang.String sql = ");

        while (m.find()) {
            final Integer idx = Integer.valueOf(m.group(1));
            ConverterInfo.SqlArg argRef = sqlParts.get(idx);

            if (argRef != null) {
                body.append("\"");
                body.append(StringEscapeUtils.escapeJava(sql.substring(startPos, m.start())));
                body.append("\" + $");
                body.append(argRef.argIdx + 1);
                body.append(" + ");

                startPos = m.end();
            }
        }

        body.append("\"");
        body.append(StringEscapeUtils.escapeJava(sql.substring(startPos)));
        body.append("\";\n");
    }
}
