package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark with the annotation a method parameter to substitute in runtime a part of SQL. Value of the annotation represent an indexed
 * argument in SQL query. Indexed argument represented with '<code>{&lt;index&gt;}</code>' construction. Argument should be iterable or
 * array type.
 * <p>
 * Example:
 * <pre><code>
 *     interface ITestAH extends IAH {
 *         {@linkplain Sql @Sql}("SELECT t.name FROM `table` t WHERE t.id IN ({0})")
 *         List&lt;String&gt; getTableNames({@linkplain SqlPart @SqlPart} @SqlVarArg int[] ids) throws {@linkplain org.xblackcat.sjpu.storage.StorageException StorageException};
 *     }
 * </code></pre>
 * <p>
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface SqlVarArg {
    String value() default "?";

    String concatBy() default ",";
}
