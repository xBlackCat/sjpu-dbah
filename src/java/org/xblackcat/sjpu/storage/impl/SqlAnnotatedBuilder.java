package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xblackcat.sjpu.storage.IRowConsumer;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.converter.StandardMappers;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class SqlAnnotatedBuilder extends AMethodBuilder<Sql> {
    public SqlAnnotatedBuilder(ClassPool pool, TypeMapper typeMapper) {
        super(typeMapper, pool);
    }

    @Override
    public void buildMethod(
            CtClass accessHelper, Method m, Sql annotation
    ) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final String methodName = m.getName();

        final Class<?> returnType = m.getReturnType();

        ConverterInfo converterInfo = ConverterInfo.analyse(pool, typeMapper, m);
        final Class<?> realReturnType = converterInfo.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = converterInfo.getConverter();

        final String sql = annotation.value();
        final QueryType type;
        {
            final Matcher matcher = BuilderUtils.FIRST_WORD_SQL.matcher(sql);
            if (matcher.find()) {
                final String word = matcher.group(1);
                if ("select".equalsIgnoreCase(word)) {
                    type = QueryType.Select;
                } else if ("insert".equalsIgnoreCase(word)) {
                    type = QueryType.Insert;
                } else if ("update".equalsIgnoreCase(word)) {
                    type = QueryType.Update;
                } else {
                    type = QueryType.Other;
                }
            } else {
                type = QueryType.Other;
            }
        }

        if (type == QueryType.Select && converter == null) {
            throw new StorageSetupException("Converter should be specified for SELECT statement in method " + m.toString());
        }

        CtClass ctRealReturnType = pool.get(returnType.getName());
        final StringBuilder body = new StringBuilder("{\n");

        Integer consumerParamIdx = null;
        List<Class<?>> parameterTypes = new ArrayList<>();
        Class<?>[] types = m.getParameterTypes();
        int i = 0;
        while (i < types.length) {
            Class<?> t = types[i];
            if (IRowConsumer.class.isAssignableFrom(t)) {
                if (consumerParamIdx != null) {
                    throw new StorageSetupException("Only one consumer could be specified for method. " + m.toString());
                }

                consumerParamIdx = i;
            } else {
                parameterTypes.add(t);
            }
            i++;
        }

        final CtClass targetReturnType;

        final boolean generateWrapper = (type == QueryType.Select || type == QueryType.Insert) &&
                (returnType.isPrimitive() && returnType != void.class);

        if (generateWrapper) {
            assert ctRealReturnType instanceof CtPrimitiveType;
            targetReturnType = pool.get(((CtPrimitiveType) ctRealReturnType).getWrapperName());
        } else {
            targetReturnType = ctRealReturnType;
        }

        if (type == QueryType.Select) {
            if (log.isDebugEnabled()) {
                log.debug("Generate SELECT method " + m);
            }

            boolean returnList = returnType.isAssignableFrom(List.class) &&
                    !realReturnType.isAssignableFrom(List.class);

            BuilderUtils.initSelectReturn(pool, targetReturnType, converter, returnList, body);
        } else if (type == QueryType.Insert) {
            if (log.isDebugEnabled()) {
                log.debug("Generate INSERT method " + m);
            }

            if (returnType.isAssignableFrom(List.class) &&
                    !realReturnType.isAssignableFrom(List.class)) {
                throw new StorageSetupException(
                        "Invalid return type for insert in method " +
                                methodName +
                                "(...): " +
                                returnType.getName()
                );
            }

            // Real return type for insert
            final CtClass rrt = returnType == void.class ? null : targetReturnType;
            BuilderUtils.initInsertReturn(pool, rrt, converter, body);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Generate UPDATE method " + m);
            }

            if (returnType.equals(int.class)) {
                body.append("return helper.update(");
                ctRealReturnType = CtClass.intType;
            } else if (returnType.equals(void.class)) {
                body.append("helper.update(");
                ctRealReturnType = CtClass.voidType;
            } else {
                throw new StorageSetupException(
                        "Invalid return type for updater in method " +
                                methodName +
                                "(...): " +
                                returnType.getName()
                );
            }
        }

        body.append("\"");
        body.append(StringEscapeUtils.escapeJava(sql));
        body.append("\",\n");

        appendArgumentList(parameterTypes, body);

        body.append("\n);\n}");

        addMethod(accessHelper, m, ctRealReturnType, targetReturnType, body.toString());
    }

    protected void appendArgumentList(Collection<Class<?>> types, StringBuilder body) {
        body.append("new Object[");

        if (types.isEmpty()) {
            body.append("0]");
        } else {
            body.append("]{\n");
            int i = 0;

            for (Class<?> type : types) {
                final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(type);

                String mapperInstanceRef = typeMap == null ? null : typeMapper.getTypeMapInstanceRef(type);
                if (mapperInstanceRef != null) {
                    body.append(mapperInstanceRef);
                    body.append(".forStore($args[");
                    body.append(i);
                    body.append("])");
                } else if (Date.class.equals(type)) {
                    body.append(BuilderUtils.getName(StandardMappers.class));
                    body.append(".dateToTimestamp((");
                    body.append(BuilderUtils.getName(Date.class));
                    body.append(")$args[");
                    body.append(i);
                    body.append("])");
                } else {
                    body.append("$args[");
                    body.append(i);
                    body.append("]");
                }

                i++;
                body.append(",\n");
            }
            body.setLength(body.length() - 2);
            body.append("\n}");
        }
    }

    protected void addMethod(
            CtClass accessHelper,
            Method m,
            CtClass realReturnType,
            CtClass targetReturnType,
            String methodBody
    ) throws CannotCompileException, NotFoundException {
        final boolean generateWrapper = !targetReturnType.equals(realReturnType);

        final String methodName = m.getName();
        final Class<?>[] types = m.getParameterTypes();

        final String targetMethodName;
        final int targetModifiers;

        if (log.isTraceEnabled()) {
            log.trace("Method " + realReturnType.getName() + " " + methodName + "(...)");
            if (generateWrapper) {
                log.trace(" [ + Wrapper for unboxing ]");
            }
            log.trace("Method body: " + methodBody);
        }

        if (generateWrapper) {
            targetMethodName = "$" + methodName + "$Wrap";
            targetModifiers = Modifier.PRIVATE;
        } else {
            targetMethodName = methodName;
            targetModifiers = m.getModifiers() | Modifier.FINAL;
        }


        final CtMethod method = CtNewMethod.make(
                targetModifiers,
                targetReturnType,
                targetMethodName,
                BuilderUtils.toCtClasses(pool, types),
                BuilderUtils.toCtClasses(pool, m.getExceptionTypes()),
                methodBody,
                accessHelper
        );

        accessHelper.addMethod(method);

        if (generateWrapper) {
            final String unwrapBody = "{\n" +
                    targetReturnType.getName() +
                    " value = " +
                    targetMethodName +
                    "($$);\n" +
                    "if (value == null) {\nthrow new java.lang.NullPointerException(\"Can't unwrap null value.\");\n}\n" +
                    "return value." +
                    BuilderUtils.getUnwrapMethodName(realReturnType) +
                    "();\n}";

            final CtMethod coverMethod = CtNewMethod.make(
                    m.getModifiers() | Modifier.FINAL,
                    realReturnType,
                    methodName,
                    BuilderUtils.toCtClasses(pool, types),
                    BuilderUtils.toCtClasses(pool, m.getExceptionTypes()),
                    unwrapBody,
                    accessHelper
            );

            accessHelper.addMethod(coverMethod);
        }

        if (log.isTraceEnabled()) {
            log.trace("Result method: " + method);
        }
    }
}
