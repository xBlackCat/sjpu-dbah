package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 11.03.13 13:21
 *
 * @author xBlackCat
 */
class UpdateObjectAnnotatedBuilder implements IMethodBuilder<UpdateObject> {
    private static final Log log = LogFactory.getLog(UpdateObjectAnnotatedBuilder.class);

    @Override
    public void buildMethod(
            ClassPool pool,
            CtClass accessHelper,
            Method m,
            UpdateObject annotation
    ) throws NotFoundException, NoSuchMethodException, CannotCompileException {
        final Class<?> returnType = m.getReturnType();

        if (annotation.fields().length == 0) {
            throw new StorageSetupException("Specify a field set to process");
        }

        final CtClass ctReturnType;
        StringBuilder body = new StringBuilder("{\n");

        if (log.isDebugEnabled()) {
            log.debug("Generate UPDATE method " + m);
        }

        if (returnType.equals(int.class)) {
            ctReturnType = CtClass.intType;
            body.append("return ");
        } else if (returnType.equals(Void.TYPE) || returnType.equals(Void.class)) {
            ctReturnType = CtClass.voidType;
        } else {
            throw new StorageSetupException("Invalid return type for UPDATE method " + m);
        }
        body.append("helper.update(");

        String tableName = annotation.value();

        final StringBuilder sql = new StringBuilder("UPDATE `");
        sql.append(tableName);
        sql.append("` SET ");

        final StringBuilder parameters = new StringBuilder();

        for (SetField attribute : annotation.fields()) {
            BuilderUtils.addStringifiedParameter(parameters, attribute);

            sql.append("`");
            sql.append(attribute.value());
            sql.append("` = ?, ");

            parameters.append(",\n");
        }

        final Class<?>[] types = m.getParameterTypes();
        final Annotation[][] parameterAnnotations = m.getParameterAnnotations();
        int typesAmount = types.length;

        Set<Integer> check = new HashSet<>();
        int i = 0;
        while (i < typesAmount) {
            for (Annotation a : parameterAnnotations[i]) {
                if (a instanceof SetField) {
                    BuilderUtils.addArgumentParameter(parameters, i, types[i]);

                    sql.append("`");
                    sql.append(((SetField) a).value());
                    sql.append("` = ?, ");

                    parameters.append(",\n");

                    check.add(i);
                    break;
                }
            }

            i++;
        }
        sql.setLength(sql.length() - 2);

        final StringBuilder where = new StringBuilder();
        for (SetField attribute : annotation.filterBy()) {
            BuilderUtils.addStringifiedParameter(parameters, attribute);

            where.append("`");
            where.append(attribute.value());
            where.append("` = ?, ");

            parameters.append(",\n");
        }

        i = 0;
        while (i < typesAmount) {
            for (Annotation a : parameterAnnotations[i]) {
                if (a instanceof QueryField) {
                    BuilderUtils.addArgumentParameter(parameters, i, types[i]);

                    where.append("`");
                    where.append(((QueryField) a).value());
                    where.append("` = ? AND ");

                    parameters.append(",\n");

                    check.add(i);
                    break;
                }
            }

            i++;
        }

        if (parameters.length() == 0) {
            throw new StorageSetupException("UPDATE query without any datas in " + m);
        }

        if (check.size() < typesAmount) {
            throw new StorageSetupException("There is not annotated parameter exist in method " + m);
        }

        if (where.length() > 0) {
            sql.append(" WHERE ");
            sql.append(where, 0, where.length() - 5);
        }

        final Limit limit = m.getAnnotation(Limit.class);
        if (limit != null) {
            sql.append(" LIMIT ");
            sql.append(limit.offset());
            sql.append(",");
            sql.append(limit.value());
        }

        body.append("\"");
        body.append(StringEscapeUtils.escapeJava(sql.toString()));
        body.append("\",\nnew Object[]{");
        body.append(parameters, 0, parameters.length() - 2);

        body.append("\n}\n);\n}");

        if (log.isTraceEnabled()) {
            log.trace("Method " + ctReturnType.getName() + " " + m.getName() + "(...)");
            log.trace("Method body: " + body.toString());
        }

        final CtMethod method = CtNewMethod.make(
                Modifier.PUBLIC | Modifier.FINAL,
                ctReturnType,
                m.getName(),
                BuilderUtils.toCtClasses(pool, types),
                BuilderUtils.toCtClasses(pool, m.getExceptionTypes()),
                body.toString(),
                accessHelper
        );

        accessHelper.addMethod(method);

        if (log.isTraceEnabled()) {
            log.trace("Result method: " + method);
        }
    }
}
