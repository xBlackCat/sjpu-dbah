package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.SingletonConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.converter.StandardMappers;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class SqlAnnotatedBuilder extends AMethodBuilder<Sql> {
    public SqlAnnotatedBuilder(ClassPool pool, TypeMapper typeMapper, Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers) {
        super(typeMapper, pool, rowSetConsumers);
    }

    @Override
    public void buildMethod(
            CtClass accessHelper, Method m, Sql annotation
    ) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final String methodName = m.getName();

        final Class<?> returnType = m.getReturnType();

        ConverterInfo info = ConverterInfo.analyse(pool, typeMapper, m);
        final Class<?> realReturnType = info.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = info.getConverter();

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

        final CtClass targetReturnType;

        final boolean generateWrapper = (type == QueryType.Select || type == QueryType.Insert) &&
                (returnType.isPrimitive() && returnType != void.class);

        if (generateWrapper) {
            assert ctRealReturnType instanceof CtPrimitiveType;
            targetReturnType = pool.get(((CtPrimitiveType) ctRealReturnType).getWrapperName());
        } else {
            targetReturnType = ctRealReturnType;
        }

        final String returnString;
        if (type == QueryType.Select) {
            if (log.isDebugEnabled()) {
                log.debug("Generate SELECT method " + m);
            }

            if (info.getConsumeIndex() == null) {
                if (returnType == void.class) {
                    throw new StorageSetupException(
                            "Consumer should be specified for select method with void return type: " + m.toString()
                    );
                }

                Class<? extends IRowSetConsumer> rowSetConsumer = hasRowSetConsumer(returnType, realReturnType);
                if (rowSetConsumer == null) {
                    rowSetConsumer = SingletonConsumer.class;
                }

                body.append(BuilderUtils.getName(IRowSetConsumer.class));
                body.append(" consumer = new ");
                body.append(BuilderUtils.getName(rowSetConsumer));
                body.append("()");

                returnString = "return (" + BuilderUtils.getName(targetReturnType) + ") consumer.getRowsHolder();\n";
            } else {
                if (returnType != void.class) {
                    throw new StorageSetupException("Consumer can't be used with non-void return type: " + m.toString());
                }

                body.append(BuilderUtils.getName(IRowConsumer.class));
                body.append(" consumer = $args[");
                body.append(info.getConsumeIndex());
                body.append("]");

                returnString = "";
            }
            body.append(";\nhelper.execute(\nconsumer,\n");

            BuilderUtils.checkConverterInstance(pool, converter);

            body.append(BuilderUtils.getName(converter));
            body.append(".Instance.I,\n");
        } else if (type == QueryType.Insert) {
            if (log.isDebugEnabled()) {
                log.debug("Generate INSERT method " + m);
            }

            if (returnType.isAssignableFrom(List.class) &&
                    !realReturnType.isAssignableFrom(List.class)) {
                throw new StorageSetupException(
                        "Invalid return type for insert in method " + methodName + "(...): " + returnType.getName()
                );
            }

            // Real return type for insert
            final CtClass rrt = returnType == void.class ? null : targetReturnType;
            BuilderUtils.initInsertReturn(pool, rrt, converter, body);

            returnString = "";
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

            returnString = "";
        }

        body.append("\"");
        body.append(StringEscapeUtils.escapeJava(sql));
        body.append("\",\n");

        appendArgumentList(info.getArgumentList(), body);

        body.append("\n);\n");
        body.append(returnString);
        body.append("}");

        addMethod(accessHelper, m, ctRealReturnType, targetReturnType, body.toString());
    }

    protected void appendArgumentList(List<ConverterInfo.Arg> types, StringBuilder body) {
        body.append("new Object[");

        if (types.isEmpty()) {
            body.append("0]");
        } else {
            body.append("]{\n");

            for (ConverterInfo.Arg arg : types) {
                Class<?> type = arg.clazz;
                final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(type);

                String mapperInstanceRef = typeMap == null ? null : typeMapper.getTypeMapInstanceRef(type);
                if (mapperInstanceRef != null) {
                    body.append(mapperInstanceRef);
                    body.append(".forStore($args[");
                    body.append(arg.idx);
                    body.append("])");
                } else if (Date.class.equals(type)) {
                    body.append(BuilderUtils.getName(StandardMappers.class));
                    body.append(".dateToTimestamp((");
                    body.append(BuilderUtils.getName(Date.class));
                    body.append(")$args[");
                    body.append(arg.idx);
                    body.append("])");
                } else {
                    body.append("$args[");
                    body.append(arg.idx);
                    body.append("]");
                }

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
