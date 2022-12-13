package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation define an object type to be mapped from result row when it is not possible detect target object type automatically.
 * For example, when a method returns a container of objects (List or Set)
 * The annotation could be specified for <ul>
 * <li>method of an interface which extends {@linkplain org.xblackcat.sjpu.storage.IAH IAH} interface</li>
 * <li>class which extends {@linkplain org.xblackcat.sjpu.storage.consumer.IRowSetConsumer IRowSetConsumer} interface to specify default
 * converter of result row to object to be consumed by the IRowSetConsumer implementation</li>
 * </ul>
 * <p>
 *
 * @see org.xblackcat.sjpu.storage.ann.ToObjectConverter
 * 07.03.13 12:12
 *
 * @author xBlackCat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MapRowTo {
    Class<?> value();
}
