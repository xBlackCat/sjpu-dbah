package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark with the annotation a method parameter to substitute in runtime a part of SQL. Value of the annotation represent an indexed
 * argument in SQL query. Indexed argument represented with '<code>{&lt;index&gt;}</code>' construction. Argument should be String type.
 * <p>
 * Example:
 * <pre><code>
 *     interface ITestAH extends IAH {
 *         {@linkplain org.xblackcat.sjpu.storage.ann.Sql @SQL}("INSERT INTO `{1}`(`name`) VALUES (?)")
 *         void addName(@SqlPart(1) String tableName, String name) throws {@linkplain org.xblackcat.sjpu.storage.StorageException StorageException};
 *     }
 * </code></pre>
 * <p>
 * SqlPart string will replace all references in SQL in runtime.
 * 17.02.14 11:21
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface SqlPart {
    int value() default 0;
}
