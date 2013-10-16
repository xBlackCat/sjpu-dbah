package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.lang.reflect.Method;
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
            ClassPool pool, CtClass accessHelper, Method m, Sql annotation
    ) throws NotFoundException, NoSuchMethodException, CannotCompileException {
        final String sql = annotation.value();
        final SqlType type;
        {
            final Matcher matcher = BuilderUtils.FIRST_WORD_SQL.matcher(sql);
            if (matcher.find()) {
                final String word = matcher.group(1);
                if ("select".equalsIgnoreCase(word)) {
                    type = SqlType.Select;
                } else if ("insert".equalsIgnoreCase(word)) {
                    type = SqlType.Insert;
                } else if ("update".equalsIgnoreCase(word)) {
                    type = SqlType.Update;
                } else {
                    type = SqlType.Other;
                }
            } else {
                type = SqlType.Other;
            }
        }

        BuilderUtils.ConverterInfo converterInfo = BuilderUtils.invoke(pool, m);
        final Class<?> realReturnType = converterInfo.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = converterInfo.getConverter();

        if (type == SqlType.Select && converter == null) {
            throw new StorageSetupException("Converter should be specified for SELECT statement in method " + m.toString());
        }

        final String methodName = m.getName();

        final Class<?> returnType = m.getReturnType();
        final Class<?>[] types = m.getParameterTypes();
        int typesAmount = types.length;

        CtClass ctReturnType = pool.get(returnType.getName());
        StringBuilder body = new StringBuilder("{\n");

        final String targetMethodName;
        final CtClass targetReturnType;
        final int targetModifiers;
        final boolean generateWrapper = type == SqlType.Select && returnType.isPrimitive();
        if (generateWrapper) {
            targetMethodName = "$" + methodName + "$Wrap";
            assert ctReturnType instanceof CtPrimitiveType;
            targetReturnType = pool.get(((CtPrimitiveType) ctReturnType).getWrapperName());
            targetModifiers = Modifier.PRIVATE;
        } else {
            targetMethodName = methodName;
            targetReturnType = ctReturnType;
            targetModifiers = Modifier.PUBLIC | Modifier.FINAL;
        }

        if (type == SqlType.Select) {
            if (log.isDebugEnabled()) {
                log.debug("Generate SELECT method " + m);
            }

            boolean returnList = returnType.isAssignableFrom(List.class) &&
                    !realReturnType.isAssignableFrom(List.class);

            BuilderUtils.initSelectReturn(pool, targetReturnType, converter, returnList, body);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Generate UPDATE method " + m);
            }

            if (returnType.equals(Integer.TYPE)) {
                body.append("return helper.update(");
                ctReturnType = CtClass.intType;
            } else if (returnType.equals(Void.TYPE)) {
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
        body.append("\",\nnew Object[");

        if (typesAmount > 0) {
            body.append("]{\n");
            int i = 0;

            while (i < typesAmount) {
                body.append("$args[");
                body.append(i);
                body.append("]");

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

        body.append("\n);\n}");

        if (log.isTraceEnabled()) {
            log.trace("Method " + ctReturnType.getName() + " " + methodName + "(...)");
            log.trace("Method body: " + body.toString());
        }

        final CtMethod method = CtNewMethod.make(
                targetModifiers,
                targetReturnType,
                targetMethodName,
                BuilderUtils.toCtClasses(pool, types),
                BuilderUtils.toCtClasses(pool, m.getExceptionTypes()),
                body.toString(),
                accessHelper
        );

        accessHelper.addMethod(method);

        if (generateWrapper) {
            final String unwrapBody = "{\n" +
                    targetReturnType.getName() +
                    " value = " +
                    targetMethodName +
                    "($$);\nreturn value." +
                    BuilderUtils.getUnwrapMethodName(returnType) +
                    "();\n}";

            final CtMethod coverMethod = CtNewMethod.make(
                    Modifier.PUBLIC | Modifier.FINAL,
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
