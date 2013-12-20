package org.xblackcat.sjpu.storage.impl;

import javassist.ClassPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;

/**
 * 19.12.13 16:50
 *
 * @author xBlackCat
 */
public abstract class AMethodBuilder<T extends Annotation> implements IMethodBuilder<T> {
    protected final Log log = LogFactory.getLog(getClass());
    protected final ClassPool pool;
    protected final TypeMapper typeMapper;

    public AMethodBuilder(
            TypeMapper typeMapper,
            ClassPool pool
    ) {
        this.typeMapper = typeMapper;
        this.pool = pool;
    }
}
