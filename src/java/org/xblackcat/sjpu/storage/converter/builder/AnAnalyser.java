package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.typemap.TypeMapper;

abstract class AnAnalyser {
    protected final TypeMapper typeMapper;

    protected AnAnalyser(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public abstract Info analyze(Class<?> clazz);

    protected boolean canProcess(Class<?> param) {
        return ConverterMethodBuilder.READ_DECLARATIONS.containsKey(param) || typeMapper.hasTypeMap(param) != null;
    }
}
