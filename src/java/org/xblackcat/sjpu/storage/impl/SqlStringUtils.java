package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xblackcat.sjpu.storage.converter.builder.Arg;
import org.xblackcat.sjpu.storage.converter.builder.ArgIdx;
import org.xblackcat.sjpu.storage.converter.builder.ArgInfo;
import org.xblackcat.sjpu.storage.converter.builder.SqlArgInfo;

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

    static List<Arg> appendSqlWithParts(StringBuilder body, String sql, Map<Integer, SqlArgInfo> sqlParts) {
        if (sqlParts.isEmpty()) {
            body.append("java.lang.String sql = \"");
            body.append(StringEscapeUtils.escapeJava(sql));
            body.append("\";\n");
            return Collections.emptyList();
        } else {
            boolean hasOptionalParts = false;
            for (SqlArgInfo arg : sqlParts.values()) {
                if (arg.sqlPart != null && arg.argIdx.optional) {
                    hasOptionalParts = true;
                    break;
                }
            }

            if (hasOptionalParts) {
                return buildStringBuilder(body, sql, sqlParts);
            } else {
                return buildConcatenation(body, sql, sqlParts);
            }
        }
    }

    private static List<Arg> buildStringBuilder(StringBuilder body, String sql, Map<Integer, SqlArgInfo> sqlParts) {
        List<Arg> optionalIndexes = new ArrayList<>();

        body.append("java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n");

        Matcher m = SQL_PART_IDX.matcher(sql);
        int startPos = 0;
        while (m.find()) {
            final Integer idx = Integer.valueOf(m.group(1));
            SqlArgInfo argRef = sqlParts.get(idx);

            if (argRef != null) {
                final ArgIdx argIdx = argRef.argIdx;

                body.append("sqlBuilder.append(\"");
                final String sqlPart = sql.substring(startPos, m.start());
                int argsAmount = getArgumentCount(sqlPart);
                while (argsAmount-- > 0) {
                    optionalIndexes.add(null);
                }
                body.append(StringEscapeUtils.escapeJava(sqlPart));
                body.append("\");\n");
                if (argRef.sqlPart == null) {
                    body.append("sqlBuilder.append($");
                    body.append(argIdx.idx + 1);
                    body.append(");\n");
                } else {
                    if (argRef.expandingType == null || argRef.expandingType.length == 0) {
                        optionalIndexes.add(new Arg(null, argIdx.idx, argIdx.optional));
                    } else {
                        for (ArgInfo ai : argRef.expandingType) {
                            optionalIndexes.add(new Arg(ai.clazz, argIdx.idx, ai.methodName, argIdx.optional));
                        }
                    }
                    if (argIdx.optional) {
                        body.append("if ($");
                        body.append(argIdx.idx + 1);
                        body.append(" != null) {\n");
                    }
                    body.append("sqlBuilder.append(\"");
                    body.append(StringEscapeUtils.escapeJava(argRef.sqlPart));
                    body.append("\");\n");
                    if (argIdx.optional) {
                        body.append("}\n");
                    }
                }

                startPos = m.end();
            }
        }

        body.append("sqlBuilder.append(\"");
        body.append(StringEscapeUtils.escapeJava(sql.substring(startPos)));
        body.append("\");\njava.lang.String sql = sqlBuilder.toString();\n");
        return optionalIndexes;
    }

    protected static List<Arg> buildConcatenation(StringBuilder body, String sql, Map<Integer, SqlArgInfo> sqlParts) {
        List<Arg> optionalIndexes = new ArrayList<>();

        Matcher m = SQL_PART_IDX.matcher(sql);
        int startPos = 0;
        body.append("java.lang.String sql = ");

        while (m.find()) {
            final Integer idx = Integer.valueOf(m.group(1));
            SqlArgInfo argRef = sqlParts.get(idx);

            if (argRef != null) {
                final ArgIdx argIdx = argRef.argIdx;
                body.append('"');
                body.append(StringEscapeUtils.escapeJava(sql.substring(startPos, m.start())));
                body.append("\" + ");
                if (argRef.sqlPart == null) {
                    body.append("$");
                    body.append(argIdx.idx + 1);
                } else {
                    optionalIndexes.add(new Arg(null, argIdx.idx, argIdx.optional));

                    body.append('"');
                    body.append(StringEscapeUtils.escapeJava(argRef.sqlPart));
                    body.append('"');
                }
                body.append(" + ");

                startPos = m.end();
            }
        }

        body.append("\"");
        body.append(StringEscapeUtils.escapeJava(sql.substring(startPos)));
        body.append("\";\n");

        return optionalIndexes;
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
