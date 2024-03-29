package org.xblackcat.sjpu.storage;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
import org.xblackcat.sjpu.storage.converter.builder.ArgIdx;
import org.xblackcat.sjpu.storage.converter.builder.ArgInfo;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * 15.11.13 14:23
 *
 * @author xBlackCat
 */
public class StorageUtils {
    private static final Object FAIL_MARKER = new Object();

    @SuppressWarnings("rawtypes")
    public static final Map<Class<?>, Class<? extends IRowSetConsumer>> DEFAULT_ROWSET_CONSUMERS = Map.of(
            List.class,
            ToListConsumer.class,
            Set.class,
            ToSetConsumer.class,
            EnumSet.class,
            ToEnumSetConsumer.class
    );
    public static final String CONVERTER_ARG_CLASS = BuilderUtils.getName(Arg.class);
    public static final String CONVERTER_ARG_IDX_CLASS = BuilderUtils.getName(ArgIdx.class);
    public static final String CONVERTER_ARG_INFO_CLASS = BuilderUtils.getName(ArgInfo.class);

    public static IConnectionFactory buildConnectionFactory(IDBConfig settings) throws GeneratorException {
        try {
            return new SimplePooledConnectionFactory(settings);
        } catch (StorageException e) {
            throw new GeneratorException("Can not initialize DB connection factory", e);
        }
    }

    public static String converterArgsToJava(Collection<Arg> args) {
        return BuilderUtils.toArrayJavaCode(StorageUtils::toJavaCode, Arg.class, args);
    }

    public static String toJavaCode(ArgIdx idx) {
        if (idx == null) {
            return "null";
        }

        return "new " + CONVERTER_ARG_IDX_CLASS + "(" + idx.idx() + ", " + idx.optional() + ")";
    }

    public static String toJavaCode(ArgInfo info) {
        if (info == null) {
            return "null";
        }

        return "new " +
               CONVERTER_ARG_INFO_CLASS +
               "(" +
               BuilderUtils.getName(info.clazz()) +
               ".class, " +
               BuilderUtils.toJavaLiteral(info.methodName()) +
               ")";
    }

    public static String toJavaCode(Arg a) {
        if (a == null) {
            return "null";
        }

        return "new " + CONVERTER_ARG_CLASS + "(" +
               BuilderUtils.getName(a.typeRawClass()) + ".class, " +
               BuilderUtils.toJavaLiteral(a.sqlPart()) + ", " +
               toJavaCode(a.varArgInfo()) + ", " +
               toJavaCode(a.idx()) + ", " +
               BuilderUtils.toArrayJavaCode(StorageUtils::toJavaCode, ArgInfo.class, a.expandedArgs()) +
               ")";
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String constructDebugSQL(String sql, Arg[] args, Object... parameters) {
        if (ArrayUtils.isEmpty(parameters)) {
            return sql;
        }

        List<DebugArg> debugArgList = expandValues(args, parameters);
        final Iterator<DebugArg> it = debugArgList.iterator();

        StringBuilder query = new StringBuilder();
        boolean inQuote = false;
        boolean escapeNext = false;
        int idx = 0;
        for (char c : sql.toCharArray()) {
            boolean insertArg = false;
            if (inQuote) {
                if (!escapeNext) {
                    switch (c) {
                        case '\\' -> escapeNext = true;
                        case '\'' -> inQuote = false;
                    }
                } else {
                    escapeNext = false;
                }
            } else {
                switch (c) {
                    case '\'' -> inQuote = true;
                    case '?' -> insertArg = it.hasNext();
                }
            }

            if (insertArg) {
                DebugArg a = it.next();
                Object value = a.value;

                query.append("/* $");
                query.append(a.idx + 1);
                if (a.arrayIdx >= 0) {
                    query.append('[');
                    query.append(a.arrayIdx);
                    query.append(']');
                }
                final ArgInfo argInfo = a.info;
                if (argInfo.methodName() != null) {
                    query.append('#');
                    query.append(argInfo.methodName());
                    query.append("()");
                }
                query.append(" = (");
                query.append(BuilderUtils.getName(argInfo.clazz()));
                query.append(")*/ ");

                // Array bound check is not necessary because of check during method generation
                if (value == FAIL_MARKER) {
                    query.append("/* Failed to resolve */ ? ");
                } else {
                    query.append(renderObject(value));
                }
            } else {
                query.append(c);
            }
        }

        return query.toString();
    }

    static List<DebugArg> expandValues(Arg[] args, Object[] parameters) {
        if (ArrayUtils.isEmpty(args)) {
            return Collections.emptyList();
        }

        final List<DebugArg> debugArgs = new ArrayList<>();
        for (Arg a : args) {
            final int idx = a.idx().idx();
            Object param = parameters[idx];
            if (a.varArgInfo() != null) {
                if (param == null) {
                    throw new NullPointerException("NULL value can't be passed to VarArg parameter");
                }
                final Iterable<?> elements;
                if (a.typeRawClass().isArray()) {
                    elements = Arrays.asList(toObjectArray(param));
                } else if (Iterable.class.isAssignableFrom(a.typeRawClass())) {
                    elements = (Iterable<?>) param;
                } else {
                    throw new IllegalArgumentException("Unexpected type for @SqlVarArg annotated parameter");
                }

                final Iterator<?> it = elements.iterator();
                if (!it.hasNext()) {
                    throw new IllegalArgumentException("Empty object set is not allowed for VarArg parameter");
                }

                if (ArrayUtils.isNotEmpty(a.expandedArgs())) {
                    int arrayIdx = 0;
                    while (it.hasNext()) {
                        Object v = it.next();
                        // process expanding
                        for (ArgInfo ai : a.expandedArgs()) {
                            Object value = getValue(v, ai.methodName());
                            debugArgs.add(new DebugArg(idx, arrayIdx, ai, value));
                        }
                        arrayIdx++;
                    }
                } else {
                    int arrayIdx = 0;
                    while (it.hasNext()) {
                        Object value = it.next();
                        debugArgs.add(new DebugArg(idx, arrayIdx, new ArgInfo(a.varArgInfo().clazz(), null), value));
                        arrayIdx++;
                    }
                }
            } else if (ArrayUtils.isNotEmpty(a.expandedArgs())) {
                // process expanding
                for (ArgInfo ai : a.expandedArgs()) {
                    Object value = getValue(param, ai.methodName());
                    debugArgs.add(new DebugArg(idx, ai, value));
                }
            } else {
                debugArgs.add(new DebugArg(idx, new ArgInfo(a.typeRawClass(), null), param));
            }
        }
        return debugArgs;
    }

    private static Object[] toObjectArray(Object param) {
        if (param instanceof Object[]) {
            return (Object[]) param;
        } else if (param instanceof long[]) {
            return ArrayUtils.toObject((long[]) param);
        } else if (param instanceof int[]) {
            return ArrayUtils.toObject((int[]) param);
        } else if (param instanceof short[]) {
            return ArrayUtils.toObject((short[]) param);
        } else if (param instanceof byte[]) {
            return ArrayUtils.toObject((byte[]) param);
        } else if (param instanceof boolean[]) {
            return ArrayUtils.toObject((boolean[]) param);
        } else if (param instanceof char[]) {
            return ArrayUtils.toObject((char[]) param);
        } else if (param instanceof double[]) {
            return ArrayUtils.toObject((double[]) param);
        } else if (param instanceof float[]) {
            return ArrayUtils.toObject((float[]) param);
        } else {
            throw new IllegalArgumentException("Unexpected parameter: " + param);
        }
    }

    private static Object getValue(Object param, String methodName) {
        if (param == null) {
            return null;
        }
        try {
            final Method method = param.getClass().getMethod(methodName);
            return method.invoke(param);
        } catch (ReflectiveOperationException e) {
            return FAIL_MARKER;
        }
    }

    protected static String renderObject(Object obj) {
        if (obj == null) {
            return "NULL";
        } else if (obj instanceof byte[] ba) {
            return toHex("0x", ba, null);
        } else if (obj instanceof String s) {
            return "'" + StringUtils.replaceEach(s, new String[]{"'", "\\"}, new String[]{"\\'", "\\\\"}) + "'";
        } else if (obj instanceof TemporalAccessor) {
            return "'" + obj + "'";
        } else {
            return obj.toString();
        }
    }

    private static String toHex(String prefix, byte[] bytes, String suffix) {
        if (bytes == null) {
            return null;
        }
        final StringBuilder toAppendTo = new StringBuilder((bytes.length << 1) + 4);
        final char[] SYMBOLS = "0123456789abcdef".toCharArray();
        if (prefix != null) {
            toAppendTo.append(prefix);
        }

        for (byte b : bytes) {
            toAppendTo.append(SYMBOLS[(b >> 4) & 0x0f]);
            toAppendTo.append(SYMBOLS[b & 0xf]);
        }

        if (suffix != null) {
            toAppendTo.append(suffix);
        }
        return toAppendTo.toString();
    }

    public static GenericObjectPool<PoolableConnection> createDefaultPool(IDBConfig settings) {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                settings.url(),
                settings.user(),
                settings.password()
        );

        final int poolSize = settings.poolSize();

        final PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setValidationQuery("SELECT 1+1");

        final GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);

        connectionPool.setMaxIdle(poolSize);
        connectionPool.setMinIdle(poolSize);
        connectionPool.setMaxTotal(poolSize);
        connectionPool.setTestOnBorrow(true);
        connectionPool.setTestWhileIdle(true);
        connectionPool.setTimeBetweenEvictionRuns(Duration.ofMillis(5000));
        connectionPool.setBlockWhenExhausted(true);
        poolableConnectionFactory.setPool(connectionPool);
        return connectionPool;
    }

    static class DebugArg {
        private final int idx;
        private final int arrayIdx;
        private final ArgInfo info;
        private final Object value;

        public DebugArg(int idx, ArgInfo info, Object value) {
            this(idx, -1, info, value);
        }

        public DebugArg(int idx, int arrayIdx, ArgInfo info, Object value) {
            this.idx = idx;
            this.arrayIdx = arrayIdx;
            this.info = info;
            this.value = value;
        }
    }
}
