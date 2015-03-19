package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assign an SQL to method. The SQL string is used as prepared statement and parameters of the method are used as arguments for
 * the prepared statement in natural order (parameters annotated {@linkplain org.xblackcat.sjpu.storage.ann.SqlPart @SqlPart} or
 * of argument type {@linkplain org.xblackcat.sjpu.storage.consumer.IRowConsumer IRowConsumer} are processed in special way)
 * <p/>
 * @see org.xblackcat.sjpu.storage.ann.SqlPart
 * @see org.xblackcat.sjpu.storage.ann.SqlOptArg
 * @see org.xblackcat.sjpu.storage.ann.ToObjectConverter
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Sql {
    String value();
}
