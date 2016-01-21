package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.converter.builder.Arg;
import org.xblackcat.sjpu.storage.converter.builder.ArgIdx;
import org.xblackcat.sjpu.storage.converter.builder.ArgInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 14.06.2014 23:37
 *
 * @author xBlackCat
 */
public class SqlStringUtils {
    private final static Pattern SQL_PART_IDX = Pattern.compile("\\{(\\d+)\\}");

    static Collection<Arg> appendSqlWithParts(StringBuilder body, String sql, Collection<Arg> staticArgs, Map<Integer, Arg> sqlParts) {
        if (sqlParts.isEmpty()) {
            body.append("java.lang.String sql = \"");
            body.append(StringEscapeUtils.escapeJava(sql));
            body.append("\";\n");
            return staticArgs;
        } else {
            boolean hasDynamicParts = false;
            for (Arg arg : sqlParts.values()) {
                if (arg.isDynamic()) {
                    hasDynamicParts = true;
                    break;
                }
            }

            if (hasDynamicParts) {
                return buildStringBuilder(body, sql, staticArgs, sqlParts);
            } else {
                return buildConcatenation(body, sql, staticArgs, sqlParts);
            }
        }
    }

    private static List<Arg> buildStringBuilder(StringBuilder body, String sql, Collection<Arg> staticArgs, Map<Integer, Arg> sqlParts) {
        final Iterator<Arg> staticArgIt = staticArgs.iterator();
        List<Arg> fullArgsList = new ArrayList<>();

        body.append("java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n");

        Matcher m = SQL_PART_IDX.matcher(sql);
        int startPos = 0;
        while (m.find()) {
            final Integer idx = Integer.valueOf(m.group(1));
            Arg arg = sqlParts.get(idx);

            if (arg != null) {
                final ArgIdx argIdx = arg.idx;

                final String sqlPart = sql.substring(startPos, m.start());
                int argsAmount = getArgumentCount(sqlPart);
                while (argsAmount-- > 0) {
                    if (!staticArgIt.hasNext()) {
                        throw new GeneratorException("Too few static arguments to substitute query parameters");
                    }
                    fullArgsList.add(staticArgIt.next());
                }
                body.append("sqlBuilder.append(\"");
                body.append(StringEscapeUtils.escapeJava(sqlPart));
                body.append("\");\n");
                if (arg.sqlPart == null) {
                    body.append("sqlBuilder.append($");
                    body.append(argIdx.idx + 1);
                    body.append(");\n");
                } else {
                    final ArgInfo varArgInfo = arg.varArgInfo;
                    final boolean isVarArg = varArgInfo != null;
                    final boolean isArray = isVarArg && arg.typeRawClass.isArray();

                    boolean inBlock = argIdx.optional || isVarArg;
                    fullArgsList.add(arg);
                    if (inBlock) {
                        body.append("if ($");
                        body.append(argIdx.idx + 1);
                        body.append(" != null) {\n");
                    }
                    if (isVarArg) {
                        final String elementClassName = BuilderUtils.getName(varArgInfo.clazz);

                        body.append("boolean firstElement = true;\n");
                        if (isArray) {
                            body.append("for (int _i = 0; _i < $");
                            body.append(idx);
                            body.append("; _i++ ) {\n");
                            body.append(elementClassName);
                            body.append(" _el = $");
                            body.append(idx);
                            body.append("[_i];\n");
                        } else {
                            body.append("java.util.Iterator _it = $");
                            body.append(idx);
                            body.append(".iterator();\nwhile (_it.hasNext()) {\n");
                            body.append(elementClassName);
                            body.append(" _el = (");
                            body.append(elementClassName);
                            body.append(") _it.next();\n");
                        }
                    }

                    body.append("sqlBuilder.append(\"");
                    body.append(StringEscapeUtils.escapeJava(arg.sqlPart));
                    body.append("\");\n");

                    if (isVarArg) {
                        body.append("if (firstElement) {\n" +
                                            "firstElement = false;\n" +
                                            "} else {\n" +
                                            "sqlBuilder.append(\"");
                        body.append(StringEscapeUtils.escapeJava(varArgInfo.methodName));

                        body.append("\");\n" +
                                            "}\n" +
                                            "}\n");
                        body.append("if (firstElement) {\nthrow new java.lang.IllegalArgumentException(\"Empty ");
                        body.append(isArray ? "array" : "collection");
                        body.append(" for vararg argument #");
                        body.append(argIdx.idx);
                        body.append("\");\n}\n");
                    }
                    if (inBlock) {
                        if (isVarArg) {
                            body.append("} else {\nthrow new java.lang.IllegalArgumentException(\"Null ");
                            body.append(isArray ? "array" : "collection");
                            body.append(" for vararg argument #");
                            body.append(argIdx.idx);
                            body.append("\");\n");
                        }
                        body.append("}\n");
                    }
                }

                startPos = m.end();
            }
        }

        body.append("sqlBuilder.append(\"");
        final String sqlPart = sql.substring(startPos);
        body.append(StringEscapeUtils.escapeJava(sqlPart));
        body.append("\");\njava.lang.String sql = sqlBuilder.toString();\n");

        int argsAmount = getArgumentCount(sqlPart);
        while (argsAmount-- > 0) {
            if (!staticArgIt.hasNext()) {
                throw new GeneratorException("Too few static arguments to substitute query parameters");
            }
            fullArgsList.add(staticArgIt.next());
        }
        if (staticArgIt.hasNext()) {
            throw new GeneratorException("Found extra arguments to substitute query parameters");
        }

        return fullArgsList;
    }

    protected static List<Arg> buildConcatenation(StringBuilder body, String sql, Collection<Arg> staticArgs, Map<Integer, Arg> sqlParts) {
        final Iterator<Arg> staticArgIt = staticArgs.iterator();
        List<Arg> fullArgsList = new ArrayList<>();

        Matcher m = SQL_PART_IDX.matcher(sql);
        int startPos = 0;
        body.append("java.lang.String sql = ");

        while (m.find()) {
            final Integer idx = Integer.valueOf(m.group(1));
            Arg arg = sqlParts.get(idx);

            if (arg != null) {
                final ArgIdx argIdx = arg.idx;
                final String sqlPart = sql.substring(startPos, m.start());
                int argsAmount = getArgumentCount(sqlPart);
                while (argsAmount-- > 0) {
                    if (!staticArgIt.hasNext()) {
                        throw new GeneratorException("Too few static arguments to substitute query parameters");
                    }
                    fullArgsList.add(staticArgIt.next());
                }
                body.append('"');
                body.append(StringEscapeUtils.escapeJava(sqlPart));
                body.append("\" + ");
                if (arg.sqlPart == null) {
                    body.append("$");
                    body.append(argIdx.idx + 1);
                } else {
                    fullArgsList.add(arg);

                    body.append('"');
                    body.append(StringEscapeUtils.escapeJava(arg.sqlPart));
                    body.append('"');
                }
                body.append(" + ");

                startPos = m.end();
            }
        }

        body.append("\"");
        final String sqlPart = sql.substring(startPos);
        body.append(StringEscapeUtils.escapeJava(sqlPart));
        body.append("\";\n");

        int argsAmount = getArgumentCount(sqlPart);
        while (argsAmount-- > 0) {
            if (!staticArgIt.hasNext()) {
                throw new GeneratorException("Too few static arguments to substitute query parameters");
            }
            fullArgsList.add(staticArgIt.next());
        }
        if (staticArgIt.hasNext()) {
            throw new GeneratorException("Found extra arguments to substitute query parameters");
        }

        return fullArgsList;
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
