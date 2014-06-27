package org.xblackcat.sjpu.storage.ann;

import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 17.02.14 11:21
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RowSetConsumer {
    Class<? extends IRowSetConsumer<?, ?>> value();
}
