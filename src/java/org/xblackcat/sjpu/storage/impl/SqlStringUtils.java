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
public class SqlStringUtils {
    private final static Pattern SQL_PART_IDX = Pattern.compile("\\{(\\d+)\\}");

    static List<Integer> appendSqlWithParts(StringBuilder body, String sql, Map<Integer, ConverterInfo.SqlArg> sqlParts) {
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

        body.append("java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n");

        Matcher m = SQL_PART_IDX.matcher(sql);
        int startPos = 0;
        while (m.find()) {
            final Integer idx = Integer.valueOf(m.group(1));
            ConverterInfo.SqlArg argRef = sqlParts.get(idx);

            if (argRef != null) {
                body.append("sqlBuilder.append(\"");
                final String sqlPart = sql.substring(startPos, m.start());
                int argsAmount = getArgumentCount(sqlPart);
                while (argsAmount-- > 0) {
                    optionalIndexes.add(null);
                }
                optionalIndexes.add(argRef.argIdx);
                body.append(StringEscapeUtils.escapeJava(sqlPart));
                body.append("\");\n");
                if (argRef.sqlPart == null) {
                    body.append("sqlBuilder.append($");
                    body.append(argRef.argIdx + 1);
                    body.append(");\n");
                } else {
                    body.append("if ($");
                    body.append(argRef.argIdx + 1);

                    body.append(" != null) {\nsqlBuilder.append(\"");
                    body.append(StringEscapeUtils.escapeJava(argRef.sqlPart));
                    body.append("\");\n}\n");
                }

                startPos = m.end();
            }
        }

        body.append("sqlBuilder.append(\"");
        body.append(StringEscapeUtils.escapeJava(sql.substring(startPos)));
        body.append("\");\njava.lang.String sql = sqlBuilder.toString();\n");
        return optionalIndexes;
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

    /**
     * Scans prepared statement (or it part) and returns amount of argument placeholders
     *
     * @param sqlPart sql part to examine
     * @return amount of found argument placeholders
     */
    public static int getArgumentCount(String sqlPart) {
        int amount = 0;

        char quote = ' '; // Stores a open quote character. Space means a quote are closed or not yet open.
        boolean wasEscape = false;

        for (char c : sqlPart.toCharArray()) {
            if (quote == ' ') {
                if (c == '?') {
                    amount++;
                } else if (c == '`' || c == '"' || c == '\'') {
                    quote = c;
                    wasEscape = false;
                }
            } else if (wasEscape) {
                wasEscape = false;
            } else if (c == quote) {
                quote = ' ';
            } else if (c == '\\') {
                wasEscape = true;
            }
        }

        return amount;
    }
}
