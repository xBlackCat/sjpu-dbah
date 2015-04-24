package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.xblackcat.sjpu.skel.BuilderUtils;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.ConsumeException;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.StorageUtils;
import org.xblackcat.sjpu.storage.consumer.IRawProcessor;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.*;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
public class AHBuilderUtils {

    public static final Pattern FIRST_WORD_SQL = Pattern.compile("(\\w+)(\\s|$)");

    public static final String CN_java_sql_Connection = BuilderUtils.getName(Connection.class);
    public static final String CN_java_sql_PreparedStatement = BuilderUtils.getName(PreparedStatement.class);
    public static final String CN_java_sql_ResultSet = BuilderUtils.getName(ResultSet.class);
    public static final String CN_java_sql_SQLException = BuilderUtils.getName(SQLException.class);
    public static final String CN_java_sql_Statement = BuilderUtils.getName(Statement.class);

    public static final String CN_ConsumeException = BuilderUtils.getName(ConsumeException.class);
    public static final String CN_IRawProcessor = BuilderUtils.getName(IRawProcessor.class);
    public static final String CN_IRowConsumer = BuilderUtils.getName(IRowConsumer.class);
    public static final String CN_IRowSetConsumer = BuilderUtils.getName(IRowSetConsumer.class);
    public static final String CN_IToObjectConverter = BuilderUtils.getName(IToObjectConverter.class);
    public static final String CN_ITypeMap = BuilderUtils.getName(ITypeMap.class);
    public static final String CN_StorageUtils = BuilderUtils.getName(StorageUtils.class);
    public static final String CN_StorageException = BuilderUtils.getName(StorageException.class);

    private static final Map<Class<?>, String> SET_DECLARATIONS;

    static {
        Map<Class<?>, String> map = new HashMap<>();

        // Integer types
        map.put(long.class, "st.setLong(idx, %s);\n");
        map.put(
                Long.class,
                "{\njava.lang.Long tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setLong(idx, tmpVal.longValue());\n}\n}\n"
        );
        map.put(int.class, "st.setInt(idx, %s);\n");
        map.put(
                Integer.class,
                "{\njava.lang.Integer tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setInt(idx, tmpVal.intValue());\n}\n}\n"
        );
        map.put(short.class, "st.setShort(idx, %s);\n");
        map.put(
                Short.class,
                "{\njava.lang.Short tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setShort(idx, tmpVal.shortValue());\n}\n}\n"
        );
        map.put(byte.class, "st.setByte(idx, %s);\n");
        map.put(
                Byte.class,
                "{\njava.lang.Byte tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setByte(idx, tmpVal.byteValue());\n}\n}\n"
        );

        // Float types
        map.put(double.class, "st.setDouble(idx, %s);\n");
        map.put(
                Double.class,
                "{\njava.lang.Double tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setDouble(idx, tmpVal.doubleValue());\n}\n}\n"
        );
        map.put(float.class, "st.setFloat(idx, %s);\n");
        map.put(
                Float.class,
                "{\njava.lang.Float tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setFloat(idx, tmpVal.floatValue());\n}\n}\n"
        );

        // Boolean type
        map.put(boolean.class, "st.setBoolean(idx, %s);\n");
        map.put(
                Boolean.class,
                "{\njava.lang.Boolean tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setBoolean(idx, tmpVal.booleanValue());\n}\n}\n"
        );

        // Other types
        map.put(byte[].class, "st.setBytes(idx, %s);\n");
        map.put(String.class, "st.setString(idx, %s);\n");
        map.put(BigDecimal.class, "st.setBigDecimal(idx, %s);\n");

        // Time classes
        map.put(java.sql.Time.class, "st.setTime(idx, %s);\n");
        map.put(java.sql.Date.class, "st.setDate(idx, %s);\n");
        map.put(java.sql.Timestamp.class, "st.setTimestamp(idx, %s);\n");

        synchronized (AHBuilderUtils.class) {
            SET_DECLARATIONS = Collections.unmodifiableMap(map);
        }
    }

    public static void checkConverterInstance(
            ClassPool pool,
            Class<? extends IToObjectConverter<?>> converter
    ) throws NotFoundException, CannotCompileException {
        try {
            pool.get(converter.getName() + "$Instance");
        } catch (NotFoundException e) {
            // Create sub-class with the object instance for internal purposes

            final CtClass toObjectClazz = pool.get(converter.getName());
            CtClass instanceClass = toObjectClazz.makeNestedClass("Instance", true);
            CtField instanceField = CtField.make(
                    "public static final " + CN_IToObjectConverter + " I;",
                    instanceClass
            );
            instanceClass.addField(instanceField, CtField.Initializer.byNew(toObjectClazz));

            instanceClass.toClass();
        }
    }

    public static Class<? extends IToObjectConverter<?>> checkStandardClassConverter(Class<?> realReturnType) {
        if (BigDecimal.class.equals(realReturnType)) {
            return ToBigDecimalConverter.class;
        } else if (boolean.class.equals(realReturnType)) {
            return ToBooleanConverter.class;
        } else if (Boolean.class.equals(realReturnType)) {
            return ToBooleanObjectConverter.class;
        } else if (byte.class.equals(realReturnType)) {
            return ToByteConverter.class;
        } else if (Byte.class.equals(realReturnType)) {
            return ToByteObjectConverter.class;
        } else if (byte[].class.equals(realReturnType)) {
            return ToBytesConverter.class;
        } else if (double.class.equals(realReturnType)) {
            return ToDoubleConverter.class;
        } else if (Double.class.equals(realReturnType)) {
            return ToDoubleObjectConverter.class;
        } else if (float.class.equals(realReturnType)) {
            return ToFloatConverter.class;
        } else if (Float.class.equals(realReturnType)) {
            return ToFloatObjectConverter.class;
        } else if (int.class.equals(realReturnType)) {
            return ToIntConverter.class;
        } else if (Integer.class.equals(realReturnType)) {
            return ToIntObjectConverter.class;
        } else if (long.class.equals(realReturnType)) {
            return ToLongConverter.class;
        } else if (Long.class.equals(realReturnType)) {
            return ToLongObjectConverter.class;
        } else if (short.class.equals(realReturnType)) {
            return ToShortConverter.class;
        } else if (Short.class.equals(realReturnType)) {
            return ToShortObjectConverter.class;
        } else if (String.class.equals(realReturnType)) {
            return ToStringConverter.class;
        } else if (void.class.equals(realReturnType) || Void.class.equals(realReturnType)) {
            return VoidConverter.class;
        }

        return null;
    }

    protected static String setParamValue(Class<?> type, String value) {
        final String setLine = SET_DECLARATIONS.get(type);
        if (setLine == null) {
            throw new GeneratorException("Can't process type " + type.getName());
        }

        return String.format(setLine, value);
    }
}
