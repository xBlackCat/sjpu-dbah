package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;

abstract class AnAnalyser {
    protected static final Constructor<?>[] EMPTY_CONSTRUCTORS = new Constructor<?>[0];
    
    protected final TypeMapper typeMapper;

    protected AnAnalyser(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public abstract Info analyze(Class<?> clazz);

    protected boolean canProcess(Class<?> param) {
        return typeMapper.canProcess(param);
    }
}
