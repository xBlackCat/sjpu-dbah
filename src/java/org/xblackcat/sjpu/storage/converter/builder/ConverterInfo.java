package org.xblackcat.sjpu.storage.converter.builder;

import javassist.CannotCompileException;
import javassist.Modifier;
import javassist.NotFoundException;
import org.apache.commons.lang3.ArrayUtils;
import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.ann.*;
import org.xblackcat.sjpu.storage.consumer.IRawProcessor;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.impl.AHBuilderUtils;
import org.xblackcat.sjpu.storage.impl.ArgumentCounter;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.util.*;

public class ConverterInfo {
    private final Class<?> realReturnType;
    private final Class<? extends IToObjectConverter<?>> converter;
    private final Integer consumeIndex;
    private final Integer rawProcessorParamIndex;
    private final Collection<Arg> staticArgs;
    private final Map<Integer, Arg> sqlParts;

    ConverterInfo(
            Class<?> realReturnType,
            Class<? extends IToObjectConverter<?>> converter,
            Integer consumeIndex,
            Integer rawProcessorParamIndex,
            Collection<Arg> staticArgs,
            Map<Integer, Arg> parts
    ) {
        this.realReturnType = realReturnType;
        this.converter = converter;
        this.consumeIndex = consumeIndex;
        this.rawProcessorParamIndex = rawProcessorParamIndex;
        this.staticArgs = staticArgs;
        sqlParts = parts;
    }

    @SuppressWarnings("rawtypes")
    public static ConverterInfo analyse(
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Method m
    ) throws ReflectiveOperationException, NotFoundException, CannotCompileException {
        return simpleAnalyse(typeMapper, rowSetConsumers, m);
    }

    @SuppressWarnings("rawtypes")
    protected static ConverterInfo simpleAnalyse(
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers,
            Method m
    ) throws NotFoundException, CannotCompileException, ReflectiveOperationException {
        final Class<?> rawClass;
        final Class<?> proposalReturnClass;
        if (m.getGenericReturnType() instanceof final ParameterizedType returnType) {
            if (!(returnType.getRawType() instanceof Class)) {
                throw new GeneratorException("Raw type is not a class " + returnType + " in method " + m);
            }
            rawClass = (Class) returnType.getRawType();
            proposalReturnClass = BuilderUtils.detectTypeArgClass(returnType);
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
        final Map<Integer, Arg> parts = new HashMap<>();

        Map<Class<?>, List<Method>> expandingClassesInMethod = collectClassesToExpand(m);

        final Type[] parameterClasses = m.getGenericParameterTypes();
        final Annotation[][] annotations = m.getParameterAnnotations();
        {
            int i = 0;
            while (i < parameterClasses.length) {
                final Type t = parameterClasses[i];
                final Class<?> rawArgClass = getRaw(t);
                if (IRawProcessor.class.isAssignableFrom(rawArgClass)) {
                    if (rawProcessorParamIndex != null) {
                        throw new GeneratorException("Only one raw processor could be specified for method. " + m);
                    } else if (consumerParamIdx != null) {
                        throw new GeneratorException("Consumer and raw process can't be specified simultaneously for method. " + m);
                    }

                    rawProcessorParamIndex = i;
                } else if (IRowConsumer.class.isAssignableFrom(rawArgClass)) {
                    if (consumerParamIdx != null) {
                        throw new GeneratorException("Only one consumer could be specified for method. " + m);
                    } else if (rawProcessorParamIndex != null) {
                        throw new GeneratorException("Consumer and raw process can't be specified simultaneously for method. " + m);
                    }

                    consumerProposalReturnClass = BuilderUtils.detectTypeArgClass(t);
                    consumerParamIdx = i;
                } else {
                    SqlPart sqlPart = null;
                    SqlOptArg sqlOptArg = null;
                    SqlArg sqlArg = null;
                    SqlVarArg sqlVarArg = null;
                    for (Annotation a : annotations[i]) {
                        if (a instanceof SqlPart part) {
                            sqlPart = part;
                        } else if (a instanceof SqlOptArg optArg) {
                            sqlOptArg = optArg;
                        } else if (a instanceof SqlVarArg varArg) {
                            sqlVarArg = varArg;
                        } else if (a instanceof SqlArg arg) {
                            sqlArg = arg;
                        }
                    }

                    if (sqlArg != null && sqlPart != null) {
                        throw new GeneratorException("@SqlArg and @SqlPart cannot be defined simultaneously for the same parameter. " + m);
                    }
                    if (sqlOptArg != null && sqlVarArg != null) {
                        throw new GeneratorException(
                                "@SqlOptArg and @SqlVarArg cannot be defined simultaneously for the same parameter " + m
                        );
                    }

                    if (sqlArg != null) {
                        final ArgInfo[] expandedArgs = detectTypeExpanding(expandingClassesInMethod, t, rawArgClass);
                        if (expandedArgs.length > 1) {
                            throw new GeneratorException(
                                    "Optional SqlArg should be mapped to a single element. Expanded to " + expandedArgs.length +
                                    " args in " + m
                            );
                        }

                        final ArgInfo[] expandingType = expandedArgs.length == 0 ? null : expandedArgs;

                        final Arg oldVal = parts.put(sqlArg.value(), new Arg(rawArgClass, "?", new ArgIdx(i), expandingType));
                        if (oldVal != null) {
                            throw new GeneratorException(
                                    "Two arguments (" + oldVal + " and " + i + ") are referenced to the same sql part index " +
                                    sqlArg.value() + " in method " + m
                            );
                        }
                    } else if (sqlPart != null) {
                        final boolean optional;
                        final String additional;
                        final ArgInfo varArgElement;
                        final ArgInfo[] expanded;

                        if (sqlOptArg != null) {
                            varArgElement = null;
                            optional = !rawArgClass.isPrimitive();
                            additional = sqlOptArg.value();

                            expanded = expandType(m, expandingClassesInMethod, t, rawArgClass, additional);
                        } else if (sqlVarArg != null) {
                            optional = false;
                            additional = sqlVarArg.value();

                            final Class<?> varArgElementClass;
                            if (rawArgClass.isArray()) {
                                varArgElementClass = rawArgClass.getComponentType();
                            } else if (!Iterable.class.isAssignableFrom(rawArgClass)) {
                                throw new GeneratorException(
                                        "Expected array or iterable object as parameter type. Got " + t + " in method " + m
                                );
                            } else if (t instanceof ParameterizedType) {
                                varArgElementClass = BuilderUtils.detectTypeArgClass(t);
                                if (varArgElementClass == null) {
                                    throw new GeneratorException(
                                            "Failed to detect element class for parameter type" + t + " in method " + m
                                    );
                                }
                            } else {
                                throw new GeneratorException("Failed to detect element class for parameter type" + t + " in method " + m);
                            }
                            expanded = expandType(m, expandingClassesInMethod, varArgElementClass, varArgElementClass, additional);
                            varArgElement = new ArgInfo(varArgElementClass, sqlVarArg.concatBy());
                        } else {
                            if (!String.class.equals(t)) {
                                throw new GeneratorException("Only String argument types could be used as plain sql parts. " + m);
                            }
                            varArgElement = null;
                            optional = false;
                            additional = null;
                            expanded = ArgInfo.NO_ARG_INFOS;
                        }

                        final Arg oldVal = parts.put(
                                sqlPart.value(),
                                new Arg(rawArgClass, additional, varArgElement, new ArgIdx(i, optional), expanded)
                        );
                        if (oldVal != null) {
                            throw new GeneratorException(
                                    "Two arguments (" + oldVal + " and " + i + ") are referenced to the same sql part index " +
                                    sqlPart.value() + " in method " + m
                            );
                        }
                    } else if (sqlOptArg != null) {
                        throw new GeneratorException("@SqlOptArg should be specified only with @SqlPart annotation in " + m);
                    } else if (sqlVarArg != null) {
                        throw new GeneratorException("@SqlVarArg should be specified only with @SqlPart annotation in " + m);
                    } else {
                        final ArgInfo[] expandedArgs = detectTypeExpanding(expandingClassesInMethod, t, rawArgClass);

                        args.add(new Arg(rawArgClass, i, expandedArgs));
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

    private static ArgInfo[] expandType(
            Method m,
            Map<Class<?>, List<Method>> expandingClassesInMethod,
            Type t,
            Class<?> rawArgClass,
            String additional
    ) {
        ArgInfo[] expanded;
        expanded = detectTypeExpanding(expandingClassesInMethod, t, rawArgClass);
        checkExpandedArgs(m, additional, expanded);
        return expanded;
    }

    private static void checkExpandedArgs(Method m, String additional, ArgInfo[] expandedArgs) {
        final int argumentCountInAdditional = ArgumentCounter.getArgumentCount(additional);
        if (expandedArgs.length == 0) {
            if (argumentCountInAdditional != 1) {
                throw new GeneratorException(
                        "Optional Sql part should have one and only one argument. Got: " + additional + " in " + m
                );
            }
        } else if (argumentCountInAdditional != expandedArgs.length) {
            throw new GeneratorException(
                    "Optional Sql part have " + additional + " arguments to substitute and argument expanded into " +
                    expandedArgs.length + " arguments. " + m
            );
        }
    }

    protected static ArgInfo[] detectTypeExpanding(
            Map<Class<?>, List<Method>> expandingClassesInMethod,
            Type type,
            Class<?> rawArgClass
    ) {
        // Detect expanding class
        final List<Method> predefinedExpanders = findExpandingType(expandingClassesInMethod, rawArgClass);
        if (predefinedExpanders != null) {
            return expandClassWithMethods(predefinedExpanders, type);
        } else {
            final ExtractFields extractFields = searchExtractFieldAnn(rawArgClass);
            if (extractFields != null) {
                final List<Method> methodList = parseProperties(rawArgClass, extractFields.value());
                return expandClassWithMethods(methodList, type);
            } else {
                return ArgInfo.NO_ARG_INFOS;
            }
        }
    }

    private static ExtractFields searchExtractFieldAnn(Class<?> rawArgClass) {
        if (rawArgClass == null) {
            return null;
        }

        final ExtractFields extractFields = rawArgClass.getAnnotation(ExtractFields.class);
        if (extractFields != null) {
            return extractFields;
        }

        return searchExtractFieldAnn(rawArgClass.getSuperclass());
    }

    private static List<Method> findExpandingType(Map<Class<?>, List<Method>> expandingClassesInMethod, Class<?> rawArgClass) {
        if (!expandingClassesInMethod.isEmpty()) {
            for (Map.Entry<Class<?>, List<Method>> e : expandingClassesInMethod.entrySet()) {
                if (e.getKey().isAssignableFrom(rawArgClass)) {
                    return e.getValue();
                }
            }
        }
        return null;
    }

    private static ArgInfo[] expandClassWithMethods(List<Method> methods, Type t) {
        final Map<TypeVariable<?>, Class<?>> typeVariables = BuilderUtils.resolveTypeVariables(t);

        return methods.stream()
                .map(getter -> {
                    final Type type = getter.getGenericReturnType();
                    final Class<?> returnType = BuilderUtils.substituteTypeVariables(typeVariables, type);
                    if (returnType == null) {
                        throw new GeneratorException("Failed to resolve target return class for method " + getter);
                    }
                    return new ArgInfo(returnType, getter.getName());
                })
                .toArray(ArgInfo[]::new);
    }

    protected static Map<Class<?>, List<Method>> collectClassesToExpand(Method m) {
        final ExpandType[] expandTypes = m.getAnnotationsByType(ExpandType.class);
        if (ArrayUtils.isEmpty(expandTypes)) {
            return Collections.emptyMap();
        }

        final Map<Class<?>, List<Method>> map = new LinkedHashMap<>();
        for (ExpandType et : expandTypes) {
            final List<Method> methods = parseProperties(et.type(), et.fields());

            map.put(et.type(), methods);
        }
        return map;
    }

    private static List<Method> parseProperties(Class<?> aClass, String[] fields) {
        final List<Method> methods = new ArrayList<>(fields.length);
        for (String property : fields) {
            Method getter = BuilderUtils.findGetter(aClass, property);
            if (getter == null) {
                throw new GeneratorException("Invalid property/method name '" + property + "' is specified for expanding class " +
                                             aClass.getName() + ": no getter is found");
            }
            methods.add(getter);
        }
        return methods;
    }

    private static Class<?> getRaw(Type t) {
        if (t instanceof @SuppressWarnings("rawtypes")Class c) {
            return c;
        } else if (t instanceof ParameterizedType pt) {
            return getRaw(pt.getRawType());
        } else if (t instanceof GenericArrayType gat) {
            return Array.newInstance(getRaw(gat.getGenericComponentType()), 0).getClass();
        }
        throw new GeneratorException("Unexpected type " + t);
    }

    protected static Class<IToObjectConverter<?>> buildConverter(
            TypeMapper typeMapper,
            Class<?> type,
            RowMap constructorSignature
    ) throws NotFoundException, CannotCompileException {
        final AnAnalyser analyser;
        if (constructorSignature == null) {
            analyser = new DefaultAnalyzer(typeMapper);
        } else {
            analyser = new SignatureFinder(typeMapper, constructorSignature.value());
        }

        final Info info = analyser.analyze(type);

        final String converterCN = BuilderUtils.asIdentifier(type) + "Converter" + info.suffix();
        final var targetClass = IToObjectConverter.class;
        return typeMapper.getOrInitTypeMap(
                type,
                () -> new ConverterMethodBuilder(typeMapper, info.reference()).buildBody(),
                converterCN,
                targetClass
        );
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

    public Collection<Arg> getStaticArgs() {
        return staticArgs;
    }

    public Map<Integer, Arg> getSqlParts() {
        return sqlParts;
    }

}
