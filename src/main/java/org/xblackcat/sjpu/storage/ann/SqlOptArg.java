package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark with the annotation a method parameter to substitute value of the annotation as sql part if passed value to the annotated
 * parameter is not null. If the annotated parameter type is primitive - it is treated as always non-null parameter.
 * <p>
 * Example:
 * <pre><code>
 *     interface ITestAH extends IAH {
 *         {@linkplain Sql @Sql}("SELECT id FROM `table` t WHERE t.name IS NOT NULL {0}")
 *         List&lt;Integer&gt; getTableIds({@linkplain SqlPart @SqlPart} @SqlOptArg("AND t.group = ?") String tableGroup) throws {@linkplain org.xblackcat.sjpu.storage.StorageException StorageException};
 *     }
 * </code></pre>
 * In case, when passed NULL to <code>tableGroup</code> parameter the following SQL will be invoked:
 * <pre><code>SELECT id FROM `table` t WHERE t.name IS NOT NULL</code></pre>
 * If a non-null value is passed to parameter the following SQL will be invoked: <pre><code>SELECT id FROM `table` t WHERE t.name IS NOT NULL AND t.group = ?</code></pre>
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface SqlOptArg {
    String value();
}
