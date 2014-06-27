package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;

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

    public static void appendSqlWithParts(StringBuilder body, String sql, Map<Integer, ConverterInfo.SqlArg> sqlParts) {
        if (sqlParts.isEmpty()) {
            body.append("\"");
            body.append(StringEscapeUtils.escapeJava(sql));
            body.append("\"");
        } else {
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
        }
    }
}
