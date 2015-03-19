package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.xblackcat.sjpu.skel.BuilderUtils;
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

}
