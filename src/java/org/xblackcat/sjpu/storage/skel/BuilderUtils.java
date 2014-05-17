package org.xblackcat.sjpu.storage.skel;

import javassist.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.converter.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
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
            ctClasses[i] = pool.get(getName(classes[i]));
            i++;
        }

        return ctClasses;
    }

    public static CtClass toCtClass(ClassPool pool, Class<?> clazz) throws NotFoundException {
        return pool.get(getName(clazz));
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
