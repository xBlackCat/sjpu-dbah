package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.SqlPart;
import org.xblackcat.sjpu.storage.ann.SqlType;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.builder.Arg;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Method;
import java.util.Collection;
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

    protected QueryType getQueryType(Method m) {
        final SqlType type = getAnnotation(m);
        if (type == null){
            throw new GeneratorException("Functional interfaces should be annotated with @SqlType annotation");
        }
        return type.value();
    }

    protected Collection<Arg> appendDefineSql(StringBuilder body, ConverterInfo info, Method m) {
        if (!info.getSqlParts().isEmpty()) {
            throw new GeneratorException(BuilderUtils.getName(SqlPart.class) + " annotation is not allowed in functional AH");
        }

        body.append("java.lang.String sql = getSql();\n");
        return info.getStaticArgs();
    }
}
