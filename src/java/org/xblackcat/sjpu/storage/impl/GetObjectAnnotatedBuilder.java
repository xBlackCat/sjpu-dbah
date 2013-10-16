package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * 11.03.13 13:21
 *
 * @author xBlackCat
 */
class GetObjectAnnotatedBuilder implements IMethodBuilder<GetObject> {
    private static final Log log = LogFactory.getLog(GetObjectAnnotatedBuilder.class);

    @Override
    public void buildMethod(
            ClassPool pool, CtClass accessHelper, Method m, GetObject annotation
    ) throws NotFoundException, NoSuchMethodException, CannotCompileException {
        final Class<?> returnType = m.getReturnType();
        final Class<?> realReturnType;

        final String methodName = m.getName();

        BuilderUtils.ConverterInfo converterInfo = BuilderUtils.invoke(pool, m);
        realReturnType = converterInfo.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = converterInfo.getConverter();
        boolean useFieldList = converterInfo.isUseFieldList();

        if (useFieldList && annotation.fields().length == 0) {
            throw new StorageSetupException("Specify a field set to process");
        }

        boolean returnList = returnType.isAssignableFrom(List.class) &&
                !realReturnType.isAssignableFrom(List.class);

        CtClass ctReturnType = pool.get(returnType.getName());
        StringBuilder body = new StringBuilder("{\n");

        final String targetMethodName;
        final CtClass targetReturnType;
        final int targetModifiers;
        if (returnType.isPrimitive()) {
            targetMethodName = "$" + methodName + "$Wrap";
            assert ctReturnType instanceof CtPrimitiveType;
            targetReturnType = pool.get(((CtPrimitiveType) ctReturnType).getWrapperName());
            targetModifiers = Modifier.PRIVATE;
        } else {
            targetMethodName = methodName;
            targetReturnType = ctReturnType;
            targetModifiers = Modifier.PUBLIC | Modifier.FINAL;
        }

        if (log.isDebugEnabled()) {
            log.debug("Generate SELECT method " + m);
        }

        try {
            converter.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new StorageSetupException("Converter " + converter.getName() + " has no default constructor.", e);
        }

        BuilderUtils.initSelectReturn(pool, targetReturnType, converter, returnList, body);

        String tableName = annotation.value();
        String tableAlias = tableName.substring(0, 1);

        final StringBuilder sql = new StringBuilder("SELECT ");
        if (useFieldList) {
            for (QueryField field : annotation.fields()) {
                sql.append('`');
                sql.append(tableAlias);
                sql.append("`.`");
                sql.append(field.value());
                sql.append("`, ");
            }
        } else {
            Constructor<?> objectConstructor = BuilderUtils.findConstructorByAnnotatedParameter(
                    realReturnType,
                    QueryField.class
            );

            for (Annotation[] ann : objectConstructor.getParameterAnnotations()) {
                for (Annotation a : ann) {
                    if (a instanceof QueryField) {
                        sql.append('`');
                        sql.append(tableAlias);
                        sql.append("`.`");
                        sql.append(((QueryField) a).value());
                        sql.append("`, ");
                        break;
                    }
                }
            }
        }

        sql.setLength(sql.length() - 2);
        sql.append(" FROM `");
        sql.append(tableName);
        sql.append("` AS `");
        sql.append(tableAlias);
        sql.append("` ");

        StringBuilder parameters = new StringBuilder("new Object[");
        final Class<?>[] types = m.getParameterTypes();
        final Annotation[][] parameterAnnotations = m.getParameterAnnotations();
        int typesAmount = types.length;
        int filterAmount = annotation.filterBy().length;

        if (typesAmount + filterAmount > 0) {
            sql.append(" WHERE ");

            parameters.append("]{\n");

            for (SetField filter : annotation.filterBy()) {
                BuilderUtils.addStringifiedParameter(parameters, filter);

                sql.append("`");
                sql.append(tableAlias);
                sql.append("`.`");
                sql.append(filter.value());
                sql.append("` = ? AND ");

                parameters.append(",\n");
            }

            if (filterAmount > 0) {
                sql.setLength(sql.length() - 5);
                parameters.setLength(parameters.length() - 2);
                parameters.append('\n');
            }

            int i = 0;

            while (i < typesAmount) {
                Class<?> t = types[i];
                BuilderUtils.addArgumentParameter(parameters, i, t);

                sql.append("`");
                sql.append(tableAlias);
                sql.append("`.`");
                boolean found = false;
                for (Annotation a : parameterAnnotations[i]) {
                    if (a instanceof QueryField) {
                        sql.append(((QueryField) a).value());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new StorageSetupException(
                            "Method annotated with " +
                                    GetObject.class +
                                    " should have parameters annotated with " +
                                    QueryField.class
                    );
                }
                sql.append("` = ? AND ");
                parameters.append(",\n");

                i++;
            }

            if (typesAmount > 0) {
                sql.setLength(sql.length() - 5);
                parameters.setLength(parameters.length() - 2);
                parameters.append("\n");
            }
            parameters.append("}");
        } else {
            parameters.append("0]");
        }

        final OrderBy[] orderBy = annotation.orderBy();
        if (orderBy.length > 0) {
            sql.append(" ORDER BY `");
            sql.append(tableAlias);
            sql.append("`.`");
            sql.append(orderBy[0].value());
            sql.append("` ");
            sql.append(orderBy[0].direction().name().toUpperCase(Locale.ROOT));

            int i = 1;
            while (i < orderBy.length) {
                OrderBy order = orderBy[i++];

                sql.append(", `");
                sql.append(tableAlias);
                sql.append("`.`");
                sql.append(order.value());
                sql.append("` ");
                sql.append(order.direction().name().toUpperCase(Locale.ROOT));
            }
        }

        final Limit limit = m.getAnnotation(Limit.class);
        if (limit != null) {
            if (limit.value() < 0) {
                throw new StorageSetupException("Negative limit value in method " + m.getName());
            }

            sql.append(" LIMIT ");
            final Offset offset = m.getAnnotation(Offset.class);
            if (offset != null) {
                if (offset.value() < 0) {
                    throw new StorageSetupException("Negative limit value in method " + m.getName());
                }
                sql.append(offset.value());
                sql.append(",");
            }
            sql.append(limit.value());
        }

        body.append("\"");
        body.append(StringEscapeUtils.escapeJava(sql.toString()));
        body.append("\",\n");
        body.append(parameters);

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

        if (returnType.isPrimitive()) {
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
