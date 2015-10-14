package org.xblackcat.sjpu.storage.converter.builder;

import javassist.CannotCompileException;
import javassist.Modifier;
import javassist.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.skel.BuilderUtils;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.ann.*;
import org.xblackcat.sjpu.storage.consumer.IRawProcessor;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.impl.AHBuilderUtils;
import org.xblackcat.sjpu.storage.impl.SqlStringUtils;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.util.*;

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
    private final Integer rawProcessorParamIndex;
    private final Collection<Arg> argumentList;
    private final Map<Integer, SqlArg> sqlParts;

    ConverterInfo(
            Class<?> realReturnType,
            Class<? extends IToObjectConverter<?>> converter,
            Integer consumeIndex,
            Integer rawProcessorParamIndex,
            Collection<Arg> argumentList,
            Map<Integer, SqlArg> parts
    ) {
        this.realReturnType = realReturnType;
        this.converter = converter;
        this.consumeIndex = consumeIndex;
        this.rawProcessorParamIndex = rawProcessorParamIndex;
        this.argumentList = argumentList;
        sqlParts = parts;
    }

    public static ConverterInfo analyse(
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Method m
    ) throws ReflectiveOperationException, NotFoundException, CannotCompileException {
        return simpleAnalyse(typeMapper, rowSetConsumers, m);
    }

    private static Class<?> detectTypeArgClass(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
        if (typeArguments.length != 1) {
            return null;
        }
        final Type argument = typeArguments[0];
        if (!(argument instanceof Class)) {
            return null;
        }
        return (Class<?>) argument;
    }

    protected static ConverterInfo simpleAnalyse(
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Method m
    ) throws NotFoundException, CannotCompileException, ReflectiveOperationException {
        final Class<?> rawClass;
        final Class<?> proposalReturnClass;
        if (m.getGenericReturnType() instanceof ParameterizedType) {
            final ParameterizedType returnType = (ParameterizedType) m.getGenericReturnType();
            if (!(returnType.getRawType() instanceof Class)) {
                throw new GeneratorException("Raw type is not a class " + returnType + " in method " + m.toString());
            }
            rawClass = (Class) returnType.getRawType();
            proposalReturnClass = detectTypeArgClass(returnType);
        } else {
            rawClass = m.getReturnType();
            proposalReturnClass = null;
        }

        final Class<? extends IToObjectConverter<?>> converter;
        final Class<?> realReturnClass;
        Integer consumerParamIdx = null;
        Integer rawProcessorParamIndex = null;
        Class<?> consumerProposalReturnClass = null;

        final Collection<Arg> args = new ArrayList<>();
        final Map<Integer, SqlArg> parts = new HashMap<>();

        final Type[] parameterClasses = m.getGenericParameterTypes();
        final Annotation[][] anns = m.getParameterAnnotations();
        {
            int i = 0;
            while (i < parameterClasses.length) {
                final Type t = parameterClasses[i];
                final Class<?> rawArgClass = getRaw(t);
                if (IRawProcessor.class.isAssignableFrom(rawArgClass)) {
                    if (rawProcessorParamIndex != null) {
                        throw new GeneratorException("Only one raw processor could be specified for method. " + m.toString());
                    } else if (consumerParamIdx != null) {
                        throw new GeneratorException(
                                "Consumer and raw process can't be specified simultaneously for method. " +
                                        m.toString()
                        );
                    }

                    rawProcessorParamIndex = i;
                } else if (IRowConsumer.class.isAssignableFrom(rawArgClass)) {
                    if (consumerParamIdx != null) {
                        throw new GeneratorException("Only one consumer could be specified for method. " + m.toString());
                    } else if (rawProcessorParamIndex != null) {
                        throw new GeneratorException(
                                "Consumer and raw process can't be specified simultaneously for method. " +
                                        m.toString()
                        );
                    }

                    consumerProposalReturnClass = detectTypeArgClass(t);
                    consumerParamIdx = i;
                } else {
                    SqlPart sqlPart = null;
                    SqlOptArg sqlOptArg = null;
                    for (Annotation a : anns[i]) {
                        if (a instanceof SqlPart) {
                            sqlPart = (SqlPart) a;
                        } else if (a instanceof SqlOptArg) {
                            sqlOptArg = (SqlOptArg) a;
                        }
                    }

                    if (sqlPart != null) {
                        final String additional;
                        if (sqlOptArg == null) {
                            if (!String.class.equals(t)) {
                                throw new GeneratorException("Only String argument types could be used as plain sql parts. " + m);
                            }
                            additional = null;
                        } else {
                            if (rawArgClass.isPrimitive()) {
                                throw new GeneratorException("Primitive argument types can't be used as optional sql parts. " + m);
                            }
                            additional = sqlOptArg.value();
                            if (SqlStringUtils.getArgumentCount(additional) != 1) {
                                throw new GeneratorException(
                                        "Optional Sql part should have one and only one argument. Got: " +
                                                additional + " in " + m.toString()
                                );
                            }
                        }

                        final SqlArg oldVal = parts.put(sqlPart.value(), new SqlArg(additional, i));
                        if (oldVal != null) {
                            throw new GeneratorException(
                                    "Two arguments (" + oldVal + " and " + i + ") are referenced to the same sql part index " +
                                            sqlPart.value() + " in method " + m
                            );
                        }
                    } else if (sqlOptArg != null) {
                        throw new GeneratorException("@SqlOptArg should be specified only with @SqlPart annotation in " + m.toString());
                    } else {
                        args.add(new Arg(rawArgClass, i));
                    }
                }
                i++;
            }
        }

        final ToObjectConverter converterAnn = m.getAnnotation(ToObjectConverter.class);

        if (converterAnn != null) {
            converter = converterAnn.value();
            if (converter.isInterface() || Modifier.isAbstract(converter.getModifiers())) {
                throw new GeneratorException("Converter should be non-abstract class");
            }
            final Method converterMethod = converter.getMethod("convert", ResultSet.class);

            realReturnClass = converterMethod.getReturnType();
        } else {
            MapRowTo mapRowTo = m.getAnnotation(MapRowTo.class);

            boolean hasRowSetConsumer = m.getAnnotation(RowSetConsumer.class) != null;
            if (!hasRowSetConsumer) {
                for (Map.Entry<Class<?>, Class<? extends IRowSetConsumer>> cl : rowSetConsumers.entrySet()) {
                    if (cl.getKey().equals(rawClass)) {
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
                    if (consumerProposalReturnClass == null) {
                        throw new GeneratorException("Set target class with annotation " + MapRowTo.class + " for method " + m);
                    } else {
                        realReturnClass = consumerProposalReturnClass;
                    }
                } else if (hasRowSetConsumer) {
                    if (proposalReturnClass != null) {
                        realReturnClass = proposalReturnClass;
                    } else {
                        throw new GeneratorException("Set target class with annotation " + MapRowTo.class + " for method " + m);
                    }
                } else {
                    realReturnClass = rawClass;
                }

            } else {
                realReturnClass = mapRowTo.value();
                if (consumerParamIdx == null &&
                        !hasRowSetConsumer &&
                        !rawClass.isAssignableFrom(realReturnClass)) {
                    throw new GeneratorException(
                            "Mapped object " + realReturnClass.getName() + " can not be returned as " + rawClass.getName() +
                                    " from method " + m
                    );
                }
            }

            if (realReturnClass.isArray()) {
                if (realReturnClass != byte[].class) {
                    throw new GeneratorException("Invalid array component type: only array of bytes is supported as return value");
                }
            } else if (!realReturnClass.isPrimitive()) {
                if (realReturnClass.isInterface() || Modifier.isAbstract(realReturnClass.getModifiers())) {
                    throw new GeneratorException("Row could be mapped only to non-abstract class");
                }
            }

            Class<? extends IToObjectConverter<?>> standardConverter = AHBuilderUtils.checkStandardClassConverter(realReturnClass);
            final ToObjectConverter objectConverterAnn = realReturnClass.getAnnotation(ToObjectConverter.class);

            if (standardConverter != null) {
                converter = standardConverter;
            } else if (objectConverterAnn != null) {
                converter = objectConverterAnn.value();
                if (converter.isInterface() || Modifier.isAbstract(converter.getModifiers())) {
                    throw new GeneratorException("Converter should be implemented class");
                }
            } else {
                Class<? extends IToObjectConverter<?>> mapperConverter = typeMapper.getTypeMapperConverter(realReturnClass);
                if (mapperConverter != null) {
                    converter = mapperConverter;
                } else {

                    RowMap constructorSignature = m.getAnnotation(RowMap.class);

                    converter = buildConverter(typeMapper, realReturnClass, constructorSignature);
                }
            }
        }
        return new ConverterInfo(realReturnClass, converter, consumerParamIdx, rawProcessorParamIndex, args, parts);
    }

    private static Class<?> getRaw(Type t) {
        if (t instanceof Class) {
            return (Class) t;
        } else if (t instanceof ParameterizedType) {
            return getRaw(((ParameterizedType) t).getRawType());
        } else if (t instanceof GenericArrayType) {
            return Array.newInstance(getRaw(((GenericArrayType) t).getGenericComponentType())).getClass();
        }
        throw new GeneratorException("Unexpected type " + t);
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
                throw new GeneratorException(
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

    public Integer getRawProcessorParamIndex() {
        return rawProcessorParamIndex;
    }

    public Collection<Arg> getArgumentList() {
        return argumentList;
    }

    public Map<Integer, SqlArg> getSqlParts() {
        return sqlParts;
    }

    public static final class Arg {
        public final Class<?> clazz;
        public final int idx;
        public final boolean optional;
        public final String methodName;

        public Arg(Class<?> clazz, int idx) {
            this(clazz, idx, null, false);
        }

        public Arg(Class<?> clazz, int idx, String methodName) {
            this(clazz, idx, methodName, false);
        }

        public Arg(Class<?> clazz, int idx, boolean optional) {
            this(clazz, idx, null, optional);
        }

        public Arg(Class<?> clazz, int idx, String methodName, boolean optional) {
            this.clazz = clazz;
            this.idx = idx;
            this.optional = optional;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Arg arg = (Arg) o;
            return idx == arg.idx &&
                    optional == arg.optional &&
                    Objects.equals(clazz, arg.clazz) &&
                    Objects.equals(methodName, arg.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, idx, optional, methodName);
        }
    }

    public static final class SqlArg {
        public final String sqlPart;
        public final int argIdx;

        public SqlArg(String sqlPart, int argIdx) {
            this.sqlPart = sqlPart;
            this.argIdx = argIdx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SqlArg sqlArg = (SqlArg) o;
            return argIdx == sqlArg.argIdx &&
                    Objects.equals(sqlPart, sqlArg.sqlPart);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sqlPart, argIdx);
        }

        @Override
        public String toString() {
            return "SqlArg{" + "sqlPart='" + sqlPart + '\'' + ", argIdx=" + argIdx + '}';
        }
    }
}
