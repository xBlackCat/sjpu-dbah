package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.ATypeMap;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.converter.StandardMappers;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class SqlAnnotatedBuilder implements IMethodBuilder<Sql> {
    private static final Log log = LogFactory.getLog(SqlAnnotatedBuilder.class);

    @Override
    public void buildMethod(
            ClassPool pool, TypeMapper typeMapper, CtClass accessHelper, Method m, Sql annotation
    ) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final String methodName = m.getName();

        final Class<?> returnType = m.getReturnType();

        ConverterInfo converterInfo = BuilderUtils.invoke(pool, typeMapper, m);
        final Class<?> realReturnType = converterInfo.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = converterInfo.getConverter();

        final StringBuilder body = new StringBuilder();

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

        CtClass ctReturnType = pool.get(returnType.getName());
        body.append("{\n");

        final CtClass targetReturnType;

        final boolean generateWrapper = (type == QueryType.Select || type == QueryType.Insert) &&
                (returnType.isPrimitive() && returnType != void.class);

        if (generateWrapper) {
            assert ctReturnType instanceof CtPrimitiveType;
            targetReturnType = pool.get(((CtPrimitiveType) ctReturnType).getWrapperName());
        } else {
            targetReturnType = ctReturnType;
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
                ctReturnType = CtClass.intType;
            } else if (returnType.equals(void.class)) {
                body.append("helper.update(");
                ctReturnType = CtClass.voidType;
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

        appendArgumentList(typeMapper, m.getParameterTypes(), body);

        body.append("\n);\n}");

        addMethod(pool, accessHelper, m, ctReturnType, targetReturnType, body.toString());
    }

    protected void appendArgumentList(TypeMapper typeMapper, Class<?>[] types, StringBuilder body) {
        int typesAmount = types.length;
        body.append("new Object[");

        if (typesAmount > 0) {
            body.append("]{\n");
            int i = 0;

            while (i < typesAmount) {
                ATypeMap<?, ?> mapperClass = typeMapper.hasTypeMap(types[i]);
                if (mapperClass == null) {
                    body.append("$args[");
                    body.append(i);
                    body.append("]");
                } else if (Date.class.equals(types[i])) {
                    body.append(BuilderUtils.getName(StandardMappers.class));
                    body.append(".dateToTimestamp($args[");
                    body.append(i);
                    body.append("])");
                } else {
                    body.append(BuilderUtils.getName(mapperClass.getClass()));
                    body.append(".Instance.I.forStore($args[");
                    body.append(i);
                    body.append("])");
                }

                i++;

                if (i < typesAmount) {
                    body.append(",");
                }
                body.append("\n");
            }
            body.append("}");
        } else {
            body.append("0]");
        }
    }

    protected void addMethod(
            ClassPool pool,
            CtClass accessHelper,
            Method m,
            CtClass ctReturnType,
            CtClass targetReturnType,
            String methodBody
    ) throws CannotCompileException, NotFoundException {
        final boolean generateWrapper = !targetReturnType.equals(ctReturnType);

        final String methodName = m.getName();
        final Class<?>[] types = m.getParameterTypes();

        final String targetMethodName;
        final int targetModifiers;

        if (log.isTraceEnabled()) {
            log.trace("Method " + ctReturnType.getName() + " " + methodName + "(...)");
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
                    BuilderUtils.getUnwrapMethodName(targetReturnType) +
                    "();\n}";

            final CtMethod coverMethod = CtNewMethod.make(
                    m.getModifiers() | Modifier.FINAL,
                    ctReturnType,
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
