package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark with the annotation a method parameter to substitute in runtime a value into SQL. It helpfull to pass the same value to a different
 * places of SQL. Value of the annotation represent an indexed argument in SQL query. Indexed argument represented with
 * '<code>{&lt;index&gt;}</code>' construction. Argument should be String type.
 * <p>
 * Example:
 * <pre><code>
 *     interface ITestAH extends IAH {
 *         {@linkplain Sql @SQL}("INSERT INTO `duplicate`(`id`, `name`, `name_copy`) VALUES (?, {1}, {1})")
 *         void addName(int id, @SqlArg(1) String name) throws {@linkplain org.xblackcat.sjpu.storage.StorageException StorageException};
 *     }
 * </code></pre>
 * <p>
 * 17.02.14 11:21
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface SqlArg {
    int value();
}
