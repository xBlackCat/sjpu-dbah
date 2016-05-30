package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that the sql query will returns data set. It is useful for INSERT queries with int PK: by default int value for
 * INSERT queries returns amount of rows affected by query.
 *
 * @author xBlackCat
 * @see Sql
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SqlReturnsData {
}
