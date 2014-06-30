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
        }
        if (boolean.class.equals(realReturnType) || Boolean.class.equals(realReturnType)) {
            return ToBooleanObjectConverter.class;
        }
        if (byte.class.equals(realReturnType) || Byte.class.equals(realReturnType)) {
            return ToByteObjectConverter.class;
        }
        if (byte[].class.equals(realReturnType)) {
            return ToBytesConverter.class;
        }
        if (double.class.equals(realReturnType) || Double.class.equals(realReturnType)) {
            return ToDoubleObjectConverter.class;
        }
        if (float.class.equals(realReturnType) || Float.class.equals(realReturnType)) {
            return ToFloatObjectConverter.class;
        }
        if (int.class.equals(realReturnType) || Integer.class.equals(realReturnType)) {
            return ToIntObjectConverter.class;
        }
        if (long.class.equals(realReturnType) || Long.class.equals(realReturnType)) {
            return ToLongObjectConverter.class;
        }
        if (short.class.equals(realReturnType) || Short.class.equals(realReturnType)) {
            return ToShortObjectConverter.class;
        }
        if (String.class.equals(realReturnType)) {
            return ToStringConverter.class;
        }
        if (void.class.equals(realReturnType) || Void.class.equals(realReturnType)) {
            return VoidConverter.class;
        }

        return null;
    }

}
