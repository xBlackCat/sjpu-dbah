package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.builder.ArgIdx;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class SqlAnnotatedBuilder extends ASelectAnnotatedBuilder<Sql> {
    public SqlAnnotatedBuilder(TypeMapper typeMapper, Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers) {
        super(Sql.class, typeMapper, rowSetConsumers);
    }

    @Override
    protected QueryType getQueryType(Method m) {
        final String sql = getAnnotation(m).value();

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
    protected List<ArgIdx> appendDefineSql(StringBuilder body, ConverterInfo info, Method m) {
        final String sql = getAnnotation(m).value();
        return SqlStringUtils.appendSqlWithParts(body, sql, info.getSqlParts());
    }

}
