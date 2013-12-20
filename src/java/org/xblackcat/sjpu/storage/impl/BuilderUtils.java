package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.SetField;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.converter.*;
import org.xblackcat.sjpu.storage.typemap.ATypeMap;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class BuilderUtils {
    private static final Log log = LogFactory.getLog(BuilderUtils.class);

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

        checkConverterInstance(pool, converter);

        body.append(getName(converter));
        body.append(".Instance.I,\n");
    }

    static void initInsertReturn(
            ClassPool pool,
            CtClass realReturnType,
            Class<? extends IToObjectConverter<?>> converter,
            StringBuilder body
    ) throws NotFoundException, CannotCompileException {
        if (realReturnType == null) {
            body.append("// No need generated keys\n");
            body.append("helper.insert(\nnull, \n");
        } else {
            body.append("return (");
            body.append(getName(realReturnType));
            body.append(")helper.insert(\n");

            checkConverterInstance(pool, converter);

            body.append(getName(converter));
            body.append(".Instance.I,\n");
        }
    }

    private static void checkConverterInstance(
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
                    "public static final " + getName(converter) + " I;",
                    instanceClass
            );
            instanceClass.addField(instanceField, CtField.Initializer.byNew(toObjectClazz));

            instanceClass.toClass();
        }
    }

    static void addArgumentParameter(StringBuilder parameters, int i, Class<?> t) {
        parameters.append("$args[");
        parameters.append(i);
        parameters.append("]");
    }

    static Class<? extends IToObjectConverter<?>> getTypeMapperConverter(
            ClassPool pool,
            ITypeMap<?, ?> typeMap
    ) throws NotFoundException, CannotCompileException, ReflectiveOperationException {
        final String converterCN = "ToObjectConverter";
        final String realClassName = typeMap.getClass().getName();
        try {

            if (log.isTraceEnabled()) {
                log.trace("Check if the converter already exists for mapper " + realClassName);
            }

            final String converterFQN = realClassName + "$" + converterCN;
            final Class<?> aClass = Class.forName(converterFQN);

            if (IToObjectConverter.class.isAssignableFrom(aClass)) {
                if (log.isTraceEnabled()) {
                    log.trace("Converter class already exists: " + converterFQN);
                }
                return (Class<IToObjectConverter<?>>) aClass;
            } else {
                throw new StorageSetupException(
                        converterFQN + " class is already exists and it is not implements " + IToObjectConverter.class.getName()
                );
            }
        } catch (ClassNotFoundException ignore) {
            // Just build a new class
        }

        if (log.isTraceEnabled()) {
            log.trace("Build converter class for mapper " + realClassName);
        }

        StringBuilder body = new StringBuilder("{\nreturn new ");
        final Class<?> returnType = typeMap.getRealType();
        body.append(getName(returnType));
        body.append("(\n");

        Class<?> type = typeMap.getDbType();
        if (String.class.equals(type)) {
            body.append("$1.getString(1)");
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            body.append("$1.getLong(1)");
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            body.append("$1.getInt(1)");
        } else if (short.class.equals(type) || Short.class.equals(type)) {
            body.append("$1.getShort(1)");
        } else if (byte.class.equals(type) || Byte.class.equals(type)) {
            body.append("$1.getByte(1)");
        } else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            body.append("$1.getBoolean(1)");
        } else if (byte[].class.equals(type)) {
            body.append("$1.getBytes(1)");
        } else if (Date.class.equals(type)) {
            body.append("$1.getTimeStamp(1)");
        } else {
            throw new StorageSetupException("Can't process type " + type.getName());
        }

        body.append("\n);\n}");

        final CtClass baseCtClass = pool.get(typeMap.getClass().getName());
        final CtClass toObjectConverter = baseCtClass.makeNestedClass(converterCN, true);

        toObjectConverter.addInterface(pool.get(IToObjectConverter.class.getName()));

        if (log.isTraceEnabled()) {
            log.trace(
                    "Generated convert method " +
                            returnType.getName() +
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

    public static String getUnwrapMethodName(CtClass returnType) {
        if (!returnType.isPrimitive()) {
            throw new StorageSetupException("Can't build unwrap method for non-primitive class.");
        }

        if (CtClass.booleanType.equals(returnType)) {
            return "booleanValue";
        }
        if (CtClass.byteType.equals(returnType)) {
            return "byteValue";
        }
        if (CtClass.doubleType.equals(returnType)) {
            return "doubleValue";
        }
        if (CtClass.floatType.equals(returnType)) {
            return "floatValue";
        }
        if (CtClass.intType.equals(returnType)) {
            return "intValue";
        }
        if (CtClass.longType.equals(returnType)) {
            return "longValue";
        }
        if (CtClass.shortType.equals(returnType)) {
            return "shortValue";
        }

        throw new StorageSetupException("Unsupported primitive type: " + returnType);
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

    protected static Class<? extends IToObjectConverter<?>> checkStandardClassConverter(Class<?> realReturnType) {
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
        if (Date.class.equals(realReturnType)) {
            return ToDateConverter.class;
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

    @SuppressWarnings("unchecked")
    protected static Class<IToObjectConverter<?>> initializeConverter(
            ClassPool pool,
            Constructor<?> objectConstructor,
            TypeMapper typeMapper,
            String suffix
    ) throws NotFoundException, CannotCompileException {
        Class<?> returnType = objectConstructor.getDeclaringClass();
        final String converterCN = "ToObjectConverter" + suffix;
        try {

            if (log.isTraceEnabled()) {
                log.trace("Check if the converter already exists for class " + returnType.getName());
            }

            final String converterFQN = returnType.getName() + "$" + converterCN;
            final Class<?> aClass = Class.forName(converterFQN);

            if (IToObjectConverter.class.isAssignableFrom(aClass)) {
                if (log.isTraceEnabled()) {
                    log.trace("Converter class already exists: " + converterFQN);
                }
                return (Class<IToObjectConverter<?>>) aClass;
            } else {
                throw new StorageSetupException(
                        converterFQN + " class is already exists and it is not implements " + IToObjectConverter.class
                );
            }
        } catch (ClassNotFoundException ignore) {
            // Just build a new class
        }

        if (log.isTraceEnabled()) {
            log.trace("Build converter class for class " + returnType.getName());
        }

        StringBuilder body = new StringBuilder("{\n");

        StringBuilder newObject = new StringBuilder("\nreturn new ");
        newObject.append(getName(returnType));
        newObject.append("(\n");

        final Class<?>[] parameterTypes = objectConstructor.getParameterTypes();
        int i = 0;
        int parameterTypesLength = parameterTypes.length;
        while (i < parameterTypesLength) {
            Class<?> type = parameterTypes[i];
            i++;

            final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(type);
            final Class<?> dbType;
            if (typeMap == null) {
                dbType = type;
            } else {
                dbType = typeMap.getDbType();
            }

            body.append(BuilderUtils.getName(dbType));
            body.append(" value");
            body.append(i);
            body.append(" = ");

            boolean wasNullCheck = !type.isPrimitive();

            if (long.class.equals(dbType) || Long.class.equals(dbType)) {
                body.append("$1.getLong(");
                body.append(i);
                body.append(")");
            } else if (int.class.equals(dbType) || Integer.class.equals(dbType)) {
                body.append("$1.getInt(");
                body.append(i);
                body.append(")");
            } else if (short.class.equals(dbType) || Short.class.equals(dbType)) {
                body.append("$1.getShort(");
                body.append(i);
                body.append(")");
            } else if (byte.class.equals(dbType) || Byte.class.equals(dbType)) {
                body.append("$1.getByte(");
                body.append(i);
                body.append(")");
            } else if (boolean.class.equals(dbType) || Boolean.class.equals(dbType)) {
                body.append("$1.getBoolean(");
                body.append(i);
                body.append(")");
            } else if (byte[].class.equals(dbType)) {
                body.append("$1.getBytes(");
                body.append(i);
                body.append(")");
                wasNullCheck = false;
            } else if (String.class.equals(dbType)) {
                body.append("$1.getString(");
                body.append(i);
                body.append(")");
                wasNullCheck = false;
            } else if (Date.class.equals(dbType)) {
                body.append(getName(StandardMappers.class));
                body.append(".timestampToDate($1.getTimestamp(");
                body.append(i);
                body.append("))");
                wasNullCheck = false;
            } else {
                throw new StorageSetupException("Can't process type " + dbType.getName());
            }

            body.append(";\n");
            if (wasNullCheck) {
                body.append("if ($1.wasNull()) { value");
                body.append(i);
                body.append(" = null; }\n");
            }

            if (typeMap != null) {
                body.append(BuilderUtils.getName(typeMap.getClass()));
                body.append(".Instance.I.forRead(");
            }
            newObject.append("value");
            newObject.append(i);
            if (typeMap != null) {
                newObject.append(")");
            }
            newObject.append(", ");
        }

        if (parameterTypesLength > 0) {
            newObject.setLength(newObject.length() - 2);
        }
        body.append(newObject);
        body.append("\n);\n}");

        final CtClass baseCtClass = pool.get(returnType.getName());
        final CtClass toObjectConverter = baseCtClass.makeNestedClass(converterCN, true);

        toObjectConverter.addInterface(pool.get(IToObjectConverter.class.getName()));

        if (log.isTraceEnabled()) {
            log.trace(
                    "Generated convert method " +
                            returnType.getName() +
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

    @SuppressWarnings("unchecked")
    protected static <T extends ATypeMap<?, ?>> T initializeMapper(
            ClassPool pool,
            Class<T> mapperClass
    ) throws CannotCompileException, NotFoundException, ReflectiveOperationException {

        try {
            pool.get(mapperClass.getName() + "$Instance");
        } catch (NotFoundException e) {
            // Create sub-class with the object instance for internal purposes

            final CtClass toObjectClazz = pool.get(mapperClass.getName());
            final CtClass instanceClass = toObjectClazz.makeNestedClass("Instance", true);
            CtField instanceField = CtField.make(
                    "public static final " + getName(mapperClass) + " I;",
                    instanceClass
            );
            instanceClass.addField(instanceField, CtField.Initializer.byNew(toObjectClazz));

            instanceClass.toClass();
            instanceClass.defrost();
        }

        return (T) Class.forName(mapperClass.getName() + "$Instance").getField("I").get(null);
    }
}
