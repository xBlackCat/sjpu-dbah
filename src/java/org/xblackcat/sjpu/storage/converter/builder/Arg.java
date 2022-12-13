package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Arrays;

public record Arg(
        Class<?> typeRawClass,
        String sqlPart,
        ArgInfo varArgInfo,
        ArgIdx idx,
        ArgInfo... expandedArgs
) {

    public Arg(Class<?> typeRawClass, int argIdx, ArgInfo... expandingType) {
        this(typeRawClass, null, null, new ArgIdx(argIdx), expandingType);
    }

    public Arg(Class<?> typeRawClass, String sqlPart, ArgIdx argIdx, ArgInfo... expandingType) {
        this(typeRawClass, sqlPart, null, argIdx, expandingType);
    }

    @Override
    public String toString() {
        return "Arg " + idx + " <" + typeRawClass + "> " +
               (sqlPart == null ? "" : "[" + sqlPart + "] ") +
               (varArgInfo == null ? "" : " var arg element type " + varArgInfo.clazz() + " with glue '" + varArgInfo.methodName() + "'") +
               (expandedArgs == null || expandedArgs.length == 0 ? "" : " expanded as " + Arrays.asList(expandedArgs));
    }

    public boolean isDynamic() {
        return sqlPart != null && idx.optional() || varArgInfo != null;
    }
}
