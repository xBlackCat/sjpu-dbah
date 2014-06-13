package org.xblackcat.sjpu.storage.converter.builder;

import javassist.CannotCompileException;
import javassist.Modifier;
import javassist.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.RowMap;
import org.xblackcat.sjpu.storage.ann.RowSetConsumer;
import org.xblackcat.sjpu.storage.ann.ToObjectConverter;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.skel.BuilderUtils;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 17.12.13 16:45
 *
 * @author xBlackCat
 */
public class ConverterInfo {
    private static final Log log = LogFactory.getLog(ConverterInfo.class);

    private final Class<?> realReturnType;
    private final Class<? extends IToObjectConverter<?>> converter;
    private final Integer consumeIndex;
    private final List<Arg> argumentList;

    ConverterInfo(
            Class<?> realReturnType,
            Class<? extends IToObjectConverter<?>> converter,
            Integer consumeIndex,
            List<Arg> argumentList
    ) {
        this.realReturnType = realReturnType;
        this.converter = converter;
        this.consumeIndex = consumeIndex;
        this.argumentList = argumentList;
    }

    public static ConverterInfo analyse(
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Method m
    ) throws ReflectiveOperationException, NotFoundException, CannotCompileException {
        final Class<?> returnType = m.getReturnType();
        final Class<? extends IToObjectConverter<?>> converter;
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

        if (converterAnn != null) {
            converter = converterAnn.value();
            if (converter.isInterface() || Modifier.isAbstract(converter.getModifiers())) {
                throw new StorageSetupException("Converter should be non-abstract class");
            }
            final Method converterMethod = converter.getMethod("convert", ResultSet.class);

            realReturnType = converterMethod.getReturnType();
        } else {
            MapRowTo mapRowTo = m.getAnnotation(MapRowTo.class);

            boolean hasRowSetConsumer = m.getAnnotation(RowSetConsumer.class) != null;
            if (!hasRowSetConsumer) {
                for (Map.Entry<Class<?>, Class<? extends IRowSetConsumer>> cl : rowSetConsumers.entrySet()) {
                    if (cl.getKey().equals(returnType)) {
                        hasRowSetConsumer = true;

                        if (mapRowTo == null) {
                            mapRowTo = cl.getValue().getAnnotation(MapRowTo.class);
                        }
                        break;
                    }
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
            } else if (objectConverterAnn != null) {
                converter = objectConverterAnn.value();
                if (converter.isInterface() || Modifier.isAbstract(converter.getModifiers())) {
                    throw new StorageSetupException("Converter should be implemented class");
                }
            } else {
                Class<? extends IToObjectConverter<?>> mapperConverter = typeMapper.getTypeMapperConverter(realReturnType);
                if (mapperConverter != null) {
                    converter = mapperConverter;
                } else {

                    RowMap constructorSignature = m.getAnnotation(RowMap.class);

                    converter = buildConverter(typeMapper, realReturnType, constructorSignature);
                }
            }
        }
        return new ConverterInfo(realReturnType, converter, consumerParamIdx, parameterTypes);
    }

    protected static Class<IToObjectConverter<?>> buildConverter(
            TypeMapper typeMapper,
            Class<?> realReturnType,
            RowMap constructorSignature
    ) throws NotFoundException, CannotCompileException {
        final AnAnalyser analyser;
        if (constructorSignature == null) {
            analyser = new DefaultAnalyzer(typeMapper);
        } else {
            analyser = new SignatureFinder(typeMapper, constructorSignature.value());
        }

        final Info info = analyser.analyze(realReturnType);

        final ConverterMethodBuilder builder = new ConverterMethodBuilder(typeMapper, info.reference);
        final String bodyCode = builder.buildBody();

        final String converterClassName = BuilderUtils.asIdentifier(realReturnType) + "Converter" + info.suffix;
        try {

            if (log.isTraceEnabled()) {
                log.trace("Check if the converter already exists for class " + realReturnType.getName());
            }

            final String converterFQN = IToObjectConverter.class.getName() + "$" + converterClassName;
            final Class<?> aClass = BuilderUtils.getClass(converterFQN, typeMapper.getParentPool());

            if (IToObjectConverter.class.isAssignableFrom(aClass)) {
                if (log.isTraceEnabled()) {
                    log.trace("Converter class already exists: " + converterFQN);
                }

                @SuppressWarnings("unchecked")
                final Class<IToObjectConverter<?>> converterClass = (Class<IToObjectConverter<?>>) aClass;
                return converterClass;
            } else {
                throw new StorageSetupException(
                        converterFQN + " class is already exists and it is not implements " + IToObjectConverter.class
                );
            }
        } catch (ClassNotFoundException ignore) {
            // Just build a new class
        }

        if (log.isTraceEnabled()) {
            log.trace("Build converter class for class " + realReturnType.getName());
        }

        return typeMapper.initializeToObjectConverter(IToObjectConverter.class, converterClassName, realReturnType, bodyCode);
    }

    public Class<?> getRealReturnType() {
        return realReturnType;
    }

    public Class<? extends IToObjectConverter<?>> getConverter() {
        return converter;
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
