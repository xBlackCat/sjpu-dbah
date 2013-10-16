package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.converter.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class BuilderUtils {
    public final static DateFormat SQL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final CtClass[] EMPTY_LIST = new CtClass[]{};

    static final Pattern FIRST_WORD_SQL = Pattern.compile("(\\w+)(\\s|$)");

    public static void addStringifiedParameter(StringBuilder parameters, SetField filter) throws NoSuchMethodException {
        Class<?> type = filter.type();
        final String value = filter.v();
        if (type.equals(String.class)) {
            parameters.append('"');
            parameters.append(StringEscapeUtils.escapeJava(value));
            parameters.append('"');
        } else if (Number.class.isAssignableFrom(type)) {
            try {
                @SuppressWarnings("UnusedDeclaration")
                double v = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new StorageSetupException("Invalid number format", e);
            }
            parameters.append(value);
        } else if (Boolean.class.isAssignableFrom(type)) {
            Boolean b = BooleanUtils.toBooleanObject(value);
            if (b == null) {
                throw new StorageSetupException("Invalid value for boolean operand: " + value);
            }
            parameters.append(getName(Boolean.class));
            parameters.append(".");
            if (b) {
                parameters.append("TRUE");
            } else {
                parameters.append("FALSE");
            }
        } else if (Date.class.equals(type)) {
            parameters.append("new ");
            parameters.append(getName(Date.class));
            parameters.append("(");
            if (!value.equalsIgnoreCase("now()")) {
                // parse date
                final Date date;
                try {
                    date = SQL_FORMAT.parse(value);
                } catch (ParseException e) {
                    throw new StorageSetupException("Invalid date value for field " + filter.value(), e);
                }

                parameters.append(date.getTime());
                parameters.append("l");
            }
            parameters.append(")");
        } else {
            throw new StorageSetupException("Can't process " + filter);
        }
    }

    static void initSelectReturn(
            ClassPool pool,
            CtClass realReturnType,
            Class<? extends IToObjectConverter<?>> converter,
            boolean returnList,
            StringBuilder body
    ) throws NotFoundException, CannotCompileException {
        if (returnList) {
            body.append("return helper.execute(\n");
        } else {
            body.append("return (");
            body.append(getName(realReturnType));
            body.append(")helper.executeSingle(\n");
        }

        try {
            pool.get(converter.getName() + "$Instance");
        } catch (NotFoundException e) {
            // Create sub-class with the object instance for internal purposes

            final CtClass toObjectClazz = pool.get(converter.getName());
            CtClass instanceClass = toObjectClazz.makeNestedClass("Instance", true);
            CtField instanceField = CtField.make(
                    "public static final " + getName(converter) + " I;",
                    instanceClass
            );
            instanceClass.addField(instanceField, CtField.Initializer.byNew(toObjectClazz));

            instanceClass.toClass();
        }

        body.append(getName(converter));
        body.append(".Instance.I,\n");
    }

    static void addArgumentParameter(StringBuilder parameters, int i, Class<?> t) {
        parameters.append("$args[");
        parameters.append(i);
        parameters.append("]");
    }

    static ConverterInfo invoke(
            ClassPool pool,
            Method m
    ) throws NoSuchMethodException, NotFoundException, CannotCompileException {
        final Class<?> returnType = m.getReturnType();
        final Class<? extends IToObjectConverter<?>> converter;
        final boolean useFieldList;
        final Class<?> realReturnType;

        final ToObjectConverter converterAnn = m.getAnnotation(ToObjectConverter.class);
        final MapRowTo mapRowTo = m.getAnnotation(MapRowTo.class);

        if (converterAnn != null) {
            converter = converterAnn.value();
            if (converter.isInterface() || Modifier.isAbstract(converter.getModifiers())) {
                throw new StorageSetupException("Converter should be implemented class");
            }
            final Method converterMethod = converter.getMethod("convert", ResultSet.class);

            realReturnType = converterMethod.getReturnType();
            useFieldList = true;
        } else {
            if (mapRowTo == null) {
                if (List.class.isAssignableFrom(returnType)) {
                    throw new StorageSetupException(
                            "Set target class with annotation " +
                                    MapRowTo.class +
                                    " for method " +
                                    m
                    );
                } else {
                    realReturnType = returnType;
                }
            } else {
                realReturnType = mapRowTo.value();
                if (!List.class.isAssignableFrom(returnType) &&
                        !returnType.isAssignableFrom(realReturnType)) {
                    throw new StorageSetupException(
                            "Mapped object " +
                                    realReturnType.getName() +
                                    " can not be returned as " +
                                    returnType.getName() +
                                    " from method " +
                                    m
                    );
                }
            }

            if (!realReturnType.isPrimitive()) {
                if (realReturnType.isInterface() || Modifier.isAbstract(realReturnType.getModifiers())) {
                    throw new StorageSetupException("Row could be mapped only to non-abstract class");
                }
            }

            Class<? extends IToObjectConverter<?>> standartConverter = ConverterInfo.checkStandardClassConverter(
                    realReturnType
            );
            final ToObjectConverter objectConverterAnn = realReturnType.getAnnotation(ToObjectConverter.class);

            if (realReturnType.getAnnotation(AutoConverter.class) != null) {
                converter = ConverterInfo.initializeConverter(pool, realReturnType);
                useFieldList = false;
            } else if (standartConverter != null) {
                converter = standartConverter;
                useFieldList = true;
            } else if (objectConverterAnn != null) {
                converter = objectConverterAnn.value();
                if (converter.isInterface() || Modifier.isAbstract(converter.getModifiers())) {
                    throw new StorageSetupException("Converter should be implemented class");
                }

                useFieldList = true;
            } else {
                throw new StorageSetupException(
                        "Neither return object class nor access helper method not annotated with " +
                                ToObjectConverter.class
                );
            }
        }
        return new ConverterInfo(realReturnType, converter, useFieldList);
    }

    public static Constructor<?> findConstructorByAnnotatedParameter(Class<?> clazz, Class<? extends Annotation> ann) {
        for (Constructor<?> c : clazz.getConstructors()) {
            final Annotation[][] annotations = c.getParameterAnnotations();
            boolean constructorAnnotated = annotations.length > 0;
            for (Annotation[] aa : annotations) {
                boolean parameterAnnotated = false;
                for (Annotation a : aa) {
                    if (ann.isAssignableFrom(a.getClass())) {
                        parameterAnnotated = true;
                        break;
                    }
                }

                constructorAnnotated = constructorAnnotated & parameterAnnotated;
            }

            if (constructorAnnotated) {
                return c;
            }
        }

        throw new StorageSetupException("No annotated constructors found");
    }

    /**
     * Returns full qualified name of the class in java-source form: inner class names separates with dot ('.') instead of dollar sign ('$')
     *
     * @param clazz class to get FQN
     * @return full qualified name of the class in java-source form
     */
    public static String getName(Class<?> clazz) {
        return StringUtils.replaceChars(clazz.getName(), '$', '.');
    }

    /**
     * Returns full qualified name of the class in java-source form: inner class names separates with dot ('.') instead of dollar sign ('$')
     *
     * @param clazz class to get FQN
     * @return full qualified name of the class in java-source form
     */
    public static String getName(CtClass clazz) {
        return StringUtils.replaceChars(clazz.getName(), '$', '.');
    }

    public static String getUnwrapMethodName(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            throw new StorageSetupException("Can't build unwrap method for non-primitive class.");
        }

        if (Boolean.TYPE.equals(returnType)) {
            return "booleanValue";
        }
        if (Byte.TYPE.equals(returnType)) {
            return "byteValue";
        }
        if (Double.TYPE.equals(returnType)) {
            return "doubleValue";
        }
        if (Float.TYPE.equals(returnType)) {
            return "floatValue";
        }
        if (Integer.TYPE.equals(returnType)) {
            return "intValue";
        }
        if (Long.TYPE.equals(returnType)) {
            return "longValue";
        }
        if (Short.TYPE.equals(returnType)) {
            return "shortValue";
        }

        throw new StorageSetupException("Unsupported primitive type: " + returnType);
    }

    static class ConverterInfo {
        private static final Log log = LogFactory.getLog(ConverterInfo.class);

        private final Class<?> realReturnType;
        private final Class<? extends IToObjectConverter<?>> converter;
        private final boolean useFieldList;

        private ConverterInfo(
                Class<?> realReturnType,
                Class<? extends IToObjectConverter<?>> converter,
                boolean useFieldList
        ) {
            this.realReturnType = realReturnType;
            this.converter = converter;
            this.useFieldList = useFieldList;
        }

        public Class<?> getRealReturnType() {
            return realReturnType;
        }

        public Class<? extends IToObjectConverter<?>> getConverter() {
            return converter;
        }

        public boolean isUseFieldList() {
            return useFieldList;
        }

        private static Class<? extends IToObjectConverter<?>> checkStandardClassConverter(Class<?> realReturnType) {
            if (BigDecimal.class.equals(realReturnType)) {
                return ToBigDecimalConverter.class;
            }
            if (Boolean.TYPE.equals(realReturnType) || Boolean.class.equals(realReturnType)) {
                return ToBooleanObjectConverter.class;
            }
            if (Byte.TYPE.equals(realReturnType) || Byte.class.equals(realReturnType)) {
                return ToByteObjectConverter.class;
            }
            if (byte[].class.equals(realReturnType)) {
                return ToBytesConverter.class;
            }
            if (Date.class.equals(realReturnType)) {
                return ToDateConverter.class;
            }
            if (Double.TYPE.equals(realReturnType) || Double.class.equals(realReturnType)) {
                return ToDoubleObjectConverter.class;
            }
            if (Float.TYPE.equals(realReturnType) || Float.class.equals(realReturnType)) {
                return ToFloatObjectConverter.class;
            }
            if (Integer.TYPE.equals(realReturnType) || Integer.class.equals(realReturnType)) {
                return ToIntObjectConverter.class;
            }
            if (Long.TYPE.equals(realReturnType) || Long.class.equals(realReturnType)) {
                return ToLongObjectConverter.class;
            }
            if (Short.TYPE.equals(realReturnType) || Short.class.equals(realReturnType)) {
                return ToShortObjectConverter.class;
            }
            if (String.class.equals(realReturnType)) {
                return ToStringConverter.class;
            }
            if (Void.TYPE.equals(realReturnType) || Void.class.equals(realReturnType)) {
                return VoidConverter.class;
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        private static Class<IToObjectConverter<?>> initializeConverter(
                ClassPool pool,
                Class<?> baseClass
        ) throws NotFoundException, CannotCompileException {
            try {
                if (log.isTraceEnabled()) {
                    log.trace("Check if the convertor already exists for class " + baseClass.getName());
                }

                final Class<?> aClass = Class.forName(baseClass.getName() + "$ToObjectConverter");

                if (aClass.isAssignableFrom(IToObjectConverter.class)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Convertor class already exists: " + baseClass.getName() + "$ToObjectConverter");
                    }
                    return (Class<IToObjectConverter<?>>) aClass;
                } else {
                    throw new StorageSetupException(
                            baseClass.getName() +
                                    "$ToObjectConverter class is already exists and it is not implements " +
                                    IToObjectConverter.class
                    );
                }
            } catch (ClassNotFoundException ignore) {
                // Just build a new class
            }

            if (log.isTraceEnabled()) {
                log.trace("Build converter class for class " + baseClass.getName());
            }

            Constructor<?> objectConstructor = findConstructorByAnnotatedParameter(
                    baseClass,
                    QueryField.class
            );
            StringBuilder body = new StringBuilder("{\nreturn new ");
            body.append(getName(baseClass));
            body.append("(\n");
            final Class<?>[] parameterTypes = objectConstructor.getParameterTypes();
            int i = 0;
            int parameterTypesLength = parameterTypes.length;
            while (i < parameterTypesLength) {
                Class<?> type = parameterTypes[i];
                i++;

                if (String.class.equals(type)) {
                    body.append("$1.getString(");
                    body.append(i);
                    body.append("),\n");
                } else if (Long.TYPE.equals(type) || Long.class.equals(type)) {
                    body.append("$1.getLong(");
                    body.append(i);
                    body.append("),\n");
                } else if (Integer.TYPE.equals(type) || Integer.class.equals(type)) {
                    body.append("$1.getInt(");
                    body.append(i);
                    body.append("),\n");
                } else if (Short.TYPE.equals(type) || Short.class.equals(type)) {
                    body.append("$1.getShort(");
                    body.append(i);
                    body.append("),\n");
                } else if (Byte.TYPE.equals(type) || Byte.class.equals(type)) {
                    body.append("$1.getByte(");
                    body.append(i);
                    body.append("),\n");
                } else if (Boolean.TYPE.equals(type) || Boolean.class.equals(type)) {
                    body.append("$1.getBoolean(");
                    body.append(i);
                    body.append("),\n");
                } else if (type.equals(Date.class)) {
                    body.append("new ");
                    body.append(getName(Date.class));
                    body.append("($1.getTimestamp(");
                    body.append(i);
                    body.append(").getTime()),\n");
                } else {
                    throw new StorageSetupException("Can't process type " + type.getName());
                }
            }

            body.setLength(body.length() - 2);
            body.append("\n);\n}");

            final CtClass baseCtClass = pool.get(baseClass.getName());
            final CtClass toObjectConverter = baseCtClass.makeNestedClass("ToObjectConverter", true);

            toObjectConverter.addInterface(pool.get(IToObjectConverter.class.getName()));

            if (log.isTraceEnabled()) {
                log.trace(
                        "Generated convert method " +
                                baseClass.getName() +
                                " convert(ResultSet $1) throws SQLException " +
                                body.toString()
                );
            }


            final CtMethod method = CtNewMethod.make(
                    Modifier.PUBLIC | Modifier.FINAL,
                    pool.get(Object.class.getName()),
                    "convert",
                    toCtClasses(pool, ResultSet.class),
                    toCtClasses(pool, SQLException.class),
                    body.toString(),
                    toObjectConverter
            );

            toObjectConverter.addMethod(method);

            if (log.isTraceEnabled()) {
                log.trace("Initialize subclass with object converter instance");
            }

            final Class<IToObjectConverter<?>> converterClass = (Class<IToObjectConverter<?>>) toObjectConverter.toClass();
            toObjectConverter.defrost();
            return converterClass;
        }
    }

    public static CtClass[] toCtClasses(ClassPool pool, Class<?>... classes) throws NotFoundException {
        CtClass[] ctClasses = new CtClass[classes.length];

        int i = 0;
        int classesLength = classes.length;

        while (i < classesLength) {
            ctClasses[i] = pool.get(classes[i].getName());
            i++;
        }

        return ctClasses;
    }

}
