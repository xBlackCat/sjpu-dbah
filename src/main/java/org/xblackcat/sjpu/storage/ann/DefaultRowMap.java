package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An object constructor marked with the annotation will be used as default converter from result row to object. Can be overridden by
 * {@linkplain org.xblackcat.sjpu.storage.ann.RowMap @RowMap} annotation.
 * <p>
 * 11.12.13 12:39
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR})
public @interface DefaultRowMap {
}
