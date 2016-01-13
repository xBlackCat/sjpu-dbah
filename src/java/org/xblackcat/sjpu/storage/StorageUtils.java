package org.xblackcat.sjpu.storage;

import org.apache.commons.lang3.ArrayUtils;
import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.connection.IDBConfig;
import org.xblackcat.sjpu.storage.connection.SimplePooledConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.ToEnumSetConsumer;
import org.xblackcat.sjpu.storage.consumer.ToListConsumer;
import org.xblackcat.sjpu.storage.consumer.ToSetConsumer;
import org.xblackcat.sjpu.storage.converter.builder.Arg;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;

/**
 * 15.11.13 14:23
 *
 * @author xBlackCat
 */
public class StorageUtils {
    public static final Map<Class<?>, Class<? extends IRowSetConsumer>> DEFAULT_ROWSET_CONSUMERS;
    public static final String CONVERTER_ARG_CLASS = BuilderUtils.getName(Arg.class);

    static {
        Map<Class<?>, Class<? extends IRowSetConsumer>> map = new HashMap<>();
        map.put(List.class, ToListConsumer.class);
        map.put(Set.class, ToSetConsumer.class);
        map.put(EnumSet.class, ToEnumSetConsumer.class);

        DEFAULT_ROWSET_CONSUMERS = Collections.unmodifiableMap(map);
    }

    public static IConnectionFactory buildConnectionFactory(IDBConfig settings) throws GeneratorException {
        try {
            return new SimplePooledConnectionFactory(settings);
        } catch (StorageException e) {
            throw new GeneratorException("Can not initialize DB connection factory", e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String constructDebugSQL(String sql, Object... parameters) {
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

    public static String converterArgsToJava(Collection<Arg> args) {
        StringBuilder javaCode = new StringBuilder("new ");
        javaCode.append(CONVERTER_ARG_CLASS);
        if (args != null && args.size() > 0) {
            javaCode.append("[]{");
            boolean first = true;
            for (Arg a : args) {
                if (first) {
                    first = false;
                } else {
                    javaCode.append(", ");
                }
                javaCode.append("new ");
                javaCode.append(CONVERTER_ARG_CLASS);
                javaCode.append("(");
                javaCode.append(BuilderUtils.getName(a.clazz));
                javaCode.append(".class, ");
                javaCode.append(a.argIdx.idx);
                javaCode.append(", ");
                if (a.methodName != null) {
                    javaCode.append('"');
                    javaCode.append(a.methodName);
                    javaCode.append('"');
                } else {
                    javaCode.append("null");
                }
                javaCode.append(", ");
                javaCode.append(Boolean.toString(a.argIdx.optional));
                javaCode.append(')');
            }
            javaCode.append('}');
        } else {
            javaCode.append("[0]");
        }

        return javaCode.toString();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String constructDebugSQL(String sql, Arg[] args, Object... parameters) {
        if (ArrayUtils.isEmpty(parameters)) {
            return sql;
        }

        StringBuilder query = new StringBuilder();
        boolean inQuote = false;
        boolean escapeNext = false;
        int idx = 0;
        for (char c : sql.toCharArray()) {
            boolean expand = false;
            if (inQuote) {
                if (!escapeNext) {
                    switch (c) {
                        case '\\':
                            escapeNext = true;
                            break;
                        case '\'':
                            inQuote = false;
                            break;
                    }
                } else {
                    escapeNext = false;
                }
            } else {
                switch (c) {
                    case '\'':
                        inQuote = true;
                        break;
                    case '?':
                        expand = args != null && idx < args.length;
                        break;
                }
            }

            if (expand) {
                Arg a = args[idx++];
                Object param = parameters[a.argIdx.idx];

                query.append("/* $");
                query.append(a.argIdx.idx + 1);
                if (a.methodName != null) {
                    query.append('#');
                    query.append(a.methodName);
                    query.append("()");
                }
                query.append(" = (");
                query.append(BuilderUtils.getName(a.clazz));
                query.append(")*/ ");

                // Array bound check is not necessary because of check during method generation
                if (param == null) {
                    query.append("NULL");
                } else if (a.methodName == null) {
                    query.append(renderObject(param));
                } else {
                    try {
                        final Method method = param.getClass().getMethod(a.methodName);
                        final Object result = method.invoke(param);
                        query.append(renderObject(result));
                    } catch (ReflectiveOperationException e) {
                        query.append("/* Failed to resolve */ ? ");
                    }
                }
            } else {
                query.append(c);
            }
        }

        return query.toString();
    }

    protected static String renderObject(Object obj) {
        if (obj == null) {
            return "NULL";
        } else if (obj instanceof String) {
            return "'" + obj.toString() + "'";
        } else {
            return obj.toString();
        }
    }
}
