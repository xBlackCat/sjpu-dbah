package org.xblackcat.sjpu.storage.impl;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.Modifier;
import javassist.NotFoundException;
import org.apache.commons.lang3.ArrayUtils;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.DefaultRowMap;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.RowMap;
import org.xblackcat.sjpu.storage.ann.ToObjectConverter;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 17.12.13 16:45
 *
 * @author xBlackCat
 */
class ConverterInfo {
    private final Class<?> realReturnType;
    private final Class<? extends IToObjectConverter<?>> converter;
    private final boolean useFieldList;
    private final Integer consumeIndex;
    private final List<Arg> argumentList;

    ConverterInfo(
            Class<?> realReturnType,
            Class<? extends IToObjectConverter<?>> converter,
            boolean useFieldList,
            Integer consumeIndex,
            List<Arg> argumentList
    ) {
        this.realReturnType = realReturnType;
        this.converter = converter;
        this.useFieldList = useFieldList;
        this.consumeIndex = consumeIndex;
        this.argumentList = argumentList;
    }

    static ConverterInfo analyse(
            ClassPool pool,
            TypeMapper typeMapper,
            Set<Class<?>> rowSetConsumers,
            Method m
    ) throws ReflectiveOperationException, NotFoundException, CannotCompileException {
        final Class<?> returnType = m.getReturnType();
        final Class<? extends IToObjectConverter<?>> converter;
        final boolean useFieldList;
        final Class<?> realReturnType;
        Integer consumerParamIdx = null;

        List<Arg> parameterTypes = new ArrayList<>();
        Class<?>[] types = m.getParameterTypes();
        {
            int i = 0;
            while (i < types.length) {
                Class<?> t = types[i];
                if (IRowConsumer.class.isAssignableFrom(t)) {
                    if (consumerParamIdx != null) {
                        throw new StorageSetupException("Only one consumer could be specified for method. " + m.toString());
                    }

                    consumerParamIdx = i;
                } else {
                    parameterTypes.add(new Arg(t, i));
                }
                i++;
            }
        }

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
            boolean hasRowSetConsumer = false;
            for (Class<?> cl : rowSetConsumers) {
                if (cl.isAssignableFrom(returnType)) {
                    hasRowSetConsumer = true;
                    break;
                }
            }

            if (mapRowTo == null) {
                if (consumerParamIdx != null) {
                    throw new StorageSetupException("Set target class with annotation " + MapRowTo.class + " for method " + m);
                }

                if (hasRowSetConsumer) {
                    throw new StorageSetupException("Set target class with annotation " + MapRowTo.class + " for method " + m);
                }

                realReturnType = returnType;
            } else {
                realReturnType = mapRowTo.value();
                if (consumerParamIdx == null &&
                        !hasRowSetConsumer &&
                        !returnType.isAssignableFrom(realReturnType)) {
                    throw new StorageSetupException(
                            "Mapped object " + realReturnType.getName() + " can not be returned as " + returnType.getName() +
                                    " from method " + m
                    );
                }
            }

            if (realReturnType.isArray()) {
                if (realReturnType != byte[].class) {
                    throw new StorageSetupException("Invalid array component type: only array of bytes is supported as return value");
                }
            } else if (!realReturnType.isPrimitive()) {
                if (realReturnType.isInterface() || Modifier.isAbstract(realReturnType.getModifiers())) {
                    throw new StorageSetupException("Row could be mapped only to non-abstract class");
                }
            }

            Class<? extends IToObjectConverter<?>> standardConverter = BuilderUtils.checkStandardClassConverter(realReturnType);
            final ToObjectConverter objectConverterAnn = realReturnType.getAnnotation(ToObjectConverter.class);

            if (standardConverter != null) {
                converter = standardConverter;
                useFieldList = true;
            } else if (objectConverterAnn != null) {
                converter = objectConverterAnn.value();
                if (converter.isInterface() || Modifier.isAbstract(converter.getModifiers())) {
                    throw new StorageSetupException("Converter should be implemented class");
                }

                useFieldList = true;
            } else {
                Class<? extends IToObjectConverter<?>> mapperConverter = typeMapper.getTypeMapperConverter(pool, realReturnType);
                if (mapperConverter != null) {
                    converter = mapperConverter;
                    useFieldList = false;
                } else {
                    final Constructor<?>[] constructors = realReturnType.getConstructors();
                    useFieldList = false;
                    Constructor<?> targetConstructor = null;
                    String suffix = "";

                    RowMap constructorSignature = m.getAnnotation(RowMap.class);
                    final Class<?>[] classes = constructorSignature == null ? null : constructorSignature.value();

                    if (constructors.length == 1) {
                        targetConstructor = constructors[0];
                    } else {
                        Constructor<?> def = null;

                        int i = 0;
                        int constructorsLength = constructors.length;

                        while (i < constructorsLength) {
                            Constructor<?> c = constructors[i];
                            if (c.getAnnotation(DefaultRowMap.class) != null) {
                                def = c;
                                if (classes == null) {
                                    // No annotations so just use the constructor annotated as default map
                                    break;
                                }
                            } else if (classes != null) {
                                if (ArrayUtils.isEquals(classes, c.getParameterTypes())) {
                                    targetConstructor = c;
                                    suffix = String.valueOf(i);
                                    break;
                                }
                            }
                            i++;
                        }

                        if (targetConstructor == null) {
                            if (classes != null) {
                                throw new StorageSetupException(
                                        "No constructor found in " + realReturnType.getName() + " with signature " + Arrays.asList(classes)
                                );
                            } else {
                                targetConstructor = def;
                                suffix = "Def";
                            }
                        }
                    }

                    if (targetConstructor == null) {
                        throw new StorageSetupException(
                                "Can't find a way to convert result row to object. Probably one of the following annotations should be used: " +
                                        Arrays.asList(ToObjectConverter.class, RowMap.class, MapRowTo.class)
                        );
                    }

                    if (classes != null) {
                        if (!ArrayUtils.isEquals(classes, targetConstructor.getParameterTypes())) {
                            throw new StorageSetupException(
                                    "No constructor found in " + realReturnType.getName() + " with signature " + Arrays.asList(classes)
                            );
                        }
                    }

                    converter = BuilderUtils.initializeConverter(pool, targetConstructor, typeMapper, suffix);
                }
            }
        }
        return new ConverterInfo(realReturnType, converter, useFieldList, consumerParamIdx, parameterTypes);
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

    public Integer getConsumeIndex() {
        return consumeIndex;
    }

    public List<Arg> getArgumentList() {
        return argumentList;
    }

    public static final class Arg {
        public final Class<?> clazz;
        public final int idx;

        public Arg(Class<?> clazz, int idx) {
            this.clazz = clazz;
            this.idx = idx;
        }
    }
}
