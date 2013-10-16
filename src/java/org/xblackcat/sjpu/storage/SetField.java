package org.xblackcat.sjpu.storage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 07.03.13 12:12
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface SetField {
    /**
     * Field value (in case when it needs)
     * @return field value as string
     */
    String v() default "";

    Class<?> type() default Void.class;

    /**
     * Field name
     * @return field name
     */
    String value();
}
