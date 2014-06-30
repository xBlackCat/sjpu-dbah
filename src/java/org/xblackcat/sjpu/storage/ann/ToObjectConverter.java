package org.xblackcat.sjpu.storage.ann;

import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use specified to-object-converter class for building an object from result row.
 *
 * @see org.xblackcat.sjpu.storage.converter.IToObjectConverter
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ToObjectConverter {
    Class<? extends IToObjectConverter<?>> value();
}
