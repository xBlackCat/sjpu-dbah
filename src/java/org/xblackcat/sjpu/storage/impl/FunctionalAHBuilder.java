package org.xblackcat.sjpu.storage.impl;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import org.xblackcat.sjpu.skel.BuilderUtils;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.SqlPart;
import org.xblackcat.sjpu.storage.ann.SqlType;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class FunctionalAHBuilder extends ASelectAnnotatedBuilder<SqlType> {
    public FunctionalAHBuilder(TypeMapper typeMapper, Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers) {
        super(SqlType.class, typeMapper, rowSetConsumers);
    }

    @Override
    public void buildMethod(CtClass accessHelper, Method m) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        super.buildMethod(accessHelper, m);
    }

    protected QueryType getQueryType(Method m) {
        return m.getAnnotation(getAnnotationClass()).value();
    }

    protected List<Integer> appendDefineSql(StringBuilder body, ConverterInfo info, Method m) {
        if (!info.getSqlParts().isEmpty()) {
            throw new GeneratorException(BuilderUtils.getName(SqlPart.class) + " annotation is not allowed in functional AH");
        }

        body.append("java.lang.String sql = getSql();\n");
        return Collections.emptyList();
    }
}
