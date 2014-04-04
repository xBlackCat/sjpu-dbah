package org.xblackcat.sjpu.storage.skel;

import javassist.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.converter.*;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
public class BuilderUtils {
    private static final Log log = LogFactory.getLog(BuilderUtils.class);

    public static final CtClass[] EMPTY_LIST = new CtClass[]{};

    public static final Pattern FIRST_WORD_SQL = Pattern.compile("(\\w+)(\\s|$)");

    private static final Map<Class<?>, String> readDeclarations;

    static {
        Map<Class<?>, String> map = new HashMap<>();

        // Integer types
        map.put(long.class, "long value%1$d = $1.getLong(%1$d);\n");
        map.put(
                Long.class,
                "long tmp%1$d = $1.getLong(%1$d);\njava.lang.Long value%1$d = $1.wasNull() ? null : java.lang.Long.valueOf(tmp%1$d);\n"
        );
        map.put(int.class, "int value%1$d = $1.getInt(%1$d);\n");
        map.put(
                Integer.class,
                "int tmp%1$d = $1.getInt(%1$d);\njava.lang.Integer value%1$d = $1.wasNull() ? null : java.lang.Integer.valueOf(tmp%1$d);\n"
        );
        map.put(short.class, "short value%1$d = $1.getShort(%1$d);\n");
        map.put(
                Short.class,
                "short tmp%1$d = $1.getShort(%1$d);\njava.lang.Short value%1$d = $1.wasNull() ? null : java.lang.Short.valueOf(tmp%1$d);\n"
        );
        map.put(byte.class, "byte value%1$d = $1.getByte(%1$d);\n");
        map.put(
                Byte.class,
                "byte tmp%1$d = $1.getByte(%1$d);\njava.lang.Byte value%1$d = $1.wasNull() ? null : java.lang.Byte.valueOf(tmp%1$d);\n"
        );

        // Float types
        map.put(double.class, "double value%1$d = $1.getDouble(%1$d);\n");
        map.put(
                Double.class,
                "double tmp%1$d = $1.getDouble(%1$d);\njava.lang.Double value%1$d = $1.wasNull() ? null : java.lang.Double.valueOf(tmp%1$d);\n"
        );
        map.put(float.class, "float value%1$d = $1.getFloat(%1$d);\n");
        map.put(
                Float.class,
                "float tmp%1$d = $1.getFloat(%1$d);\njava.lang.Float value%1$d = $1.wasNull() ? null : java.lang.Float.valueOf(tmp%1$d);\n"
        );

        // Boolean type
        map.put(boolean.class, "boolean value%1$d = $1.getBoolean(%1$d);\n");
        map.put(
                Boolean.class,
                "boolean tmp%1$d = $1.getBoolean(%1$d);\njava.lang.Boolean value%1$d = $1.wasNull() ? null : java.lang.Boolean.valueOf(tmp%1$d);\n"
        );

        // Other types
        map.put(byte[].class, "byte[] value%1$d = $1.getBytes(%1$d);\n");
        map.put(String.class, String.class.getName() + " value%1$d = $1.getString(%1$d);\n");
        map.put(
                Date.class,
                Date.class.getName() + " value%1$d = " + getName(StandardMappers.class) + ".timestampToDate($1.getTimestamp(%1$d));\n"
        );

        synchronized (BuilderUtils.class) {
            readDeclarations = Collections.unmodifiableMap(map);
        }
    }

    public static void initInsertReturn(
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
                    "public static final " + getName(IToObjectConverter.class) + " I;",
                    instanceClass
            );
            instanceClass.addField(instanceField, CtField.Initializer.byNew(toObjectClazz));

            instanceClass.toClass();
        }
    }

    /**
     * Returns full qualified name of the class in java-source form: inner class names separates with dot ('.') instead of dollar sign ('$')
     *
     * @param clazz class to get FQN
     * @return full qualified name of the class in java-source form
     */
    public static String getName(Class<?> clazz) {
        return StringUtils.replaceChars(checkArray(clazz), '$', '.');
    }

    protected static String checkArray(Class<?> clazz) {
        if (!clazz.isArray()) {
            return clazz.getName();
        }

        return checkArray(clazz.getComponentType()) + "[]";
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
    public static Class<IToObjectConverter<?>> initializeConverter(
            Constructor<?> objectConstructor,
            TypeMapper typeMapper,
            String suffix
    ) throws NotFoundException, CannotCompileException {
        Class<?> returnType = objectConstructor.getDeclaringClass();
        final String converterCN = asIdentifier(returnType) + "Converter" + suffix;
        try {

            if (log.isTraceEnabled()) {
                log.trace("Check if the converter already exists for class " + returnType.getName());
            }

            final String converterFQN = IToObjectConverter.class.getName() + "$" + converterCN;
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

            String declarationLine = readDeclarations.get(dbType);
            if (declarationLine == null) {
                throw new StorageSetupException("Can't process type " + dbType.getName());
            }

            body.append(String.format(declarationLine, i));

            if (typeMap != null) {
                newObject.append("(");
                newObject.append(getName(type));
                newObject.append(") ");
                newObject.append(typeMapper.getTypeMapInstanceRef(type));
                newObject.append(".forRead(");
            }
            newObject.append("value");
            newObject.append(i);
            if (typeMap != null) {
                newObject.append(")");
            }
            newObject.append(",\n");
        }

        if (parameterTypesLength > 0) {
            newObject.setLength(newObject.length() - 2);
        }
        body.append(newObject);
        body.append("\n);\n}");

        return typeMapper.initializeToObjectConverter(IToObjectConverter.class, converterCN, body, returnType);
    }

    public static String asIdentifier(Class<?> typeMap) {
        return StringUtils.replaceChars(getName(typeMap), '.', '_');
    }

    public static ClassPool getClassPool(ClassPool parent, Class<?> clazz, Class<?>... classes) {
        ClassPool pool = new ClassPool(parent);

        Set<ClassLoader> usedLoaders = new HashSet<>();
        usedLoaders.add(ClassLoader.getSystemClassLoader());
        usedLoaders.add(ClassPool.class.getClassLoader());

        if (usedLoaders.add(clazz.getClassLoader())) {
            pool.appendClassPath(new ClassClassPath(clazz));
        }

        for (Class<?> c : classes) {
            if (usedLoaders.add(c.getClassLoader())) {
                pool.appendClassPath(new ClassClassPath(c));
            }
        }

        return pool;
    }
}
