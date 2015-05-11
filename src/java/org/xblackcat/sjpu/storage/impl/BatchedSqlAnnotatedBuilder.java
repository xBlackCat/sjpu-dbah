package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.xblackcat.sjpu.skel.BuilderUtils;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.ann.SqlPart;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class BatchedSqlAnnotatedBuilder extends ASelectAnnotatedBuilder<Sql> {
    public BatchedSqlAnnotatedBuilder(TypeMapper typeMapper, Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers) {
        super(Sql.class, typeMapper, rowSetConsumers);
    }

    @Override
    public void buildMethod(CtClass accessHelper, Class<?> targetClass, Method m) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        for (Annotation[] aa : m.getParameterAnnotations()) {
            for (Annotation a : aa) {
                if (a instanceof SqlPart) {
                    throw new GeneratorException(SqlPart.class.getName() + " annotation is not allowed for batched Access Helpers");
                }
            }
        }
        // Build a field for holding initialized prepared statement object

        CtField field = new CtField(
                BuilderUtils.toCtClass(typeMapper.getParentPool(), PreparedStatement.class),
                BuilderUtils.asIdentifier(m),
                accessHelper
        );
        field.setModifiers(Modifier.PRIVATE);
        accessHelper.addField(field);

        super.buildMethod(accessHelper, targetClass, m);
    }

    @Override
    protected void appendPrepareStatement(StringBuilder body, Method m, boolean returnKeys) {
        final String identifier = BuilderUtils.asIdentifier(m);
        body.append("if (");
        body.append(identifier);
        body.append(" == null) {\n");
        body.append("try {\n");
        body.append(identifier);
        body.append(" = con.prepareStatement(\nsql,\n");
        body.append(AHBuilderUtils.CN_java_sql_Statement);
        if (returnKeys) {
            body.append(".RETURN_GENERATED_KEYS");
        } else {
            body.append(".NO_GENERATED_KEYS");
        }
        body.append(");\n} catch (");
        body.append(AHBuilderUtils.CN_java_sql_SQLException);
        body.append(" e) {\nthrow new ");
        body.append(AHBuilderUtils.CN_StorageException);
        body.append("(\"Failed to initialize prepared statement\", e);\n}\n}\n");
        body.append(AHBuilderUtils.CN_java_sql_PreparedStatement);
        body.append(" st = ");
        body.append(identifier);
        body.append(";\n");
    }

    @Override
    protected void appendCloseStatement(StringBuilder body) {
    }

    @Override
    protected QueryType getQueryType(Method m) {
        final String sql = m.getAnnotation(getAnnotationClass()).value();

        final QueryType type;
        {
            final Matcher matcher = AHBuilderUtils.FIRST_WORD_SQL.matcher(sql);
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
        return type;
    }

    @Override
    protected List<Integer> appendDefineSql(StringBuilder body, ConverterInfo info, Method m) {
        final String sql = m.getAnnotation(getAnnotationClass()).value();
        return SqlStringUtils.appendSqlWithParts(body, sql, info.getSqlParts());
    }

}
