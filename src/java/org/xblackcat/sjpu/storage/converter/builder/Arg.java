package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Arrays;
import java.util.Objects;

/**
 * 13.01.2016 11:14
 *
 * @author xBlackCat
 */
public final class Arg {
    public final Class<?> typeRawClass;
    public final String sqlPart;
    public final ArgInfo varArgInfo;
    public final ArgIdx idx;
    public final ArgInfo[] expandedArgs;

    public Arg(Class<?> typeRawClass, int argIdx, ArgInfo... expandingType) {
        this(typeRawClass, null, null, new ArgIdx(argIdx), expandingType);
    }

    public Arg(Class<?> typeRawClass, String sqlPart, ArgIdx argIdx, ArgInfo... expandingType) {
        this(typeRawClass, sqlPart, null, argIdx, expandingType);
    }

    public Arg(
            Class<?> typeRawClass,
            String sqlPart,
            ArgInfo varArgInfo,
            ArgIdx argIdx,
            ArgInfo... expandedArgs
    ) {
        this.typeRawClass = typeRawClass;
        this.sqlPart = sqlPart;
        this.varArgInfo = varArgInfo;
        idx = argIdx;
        this.expandedArgs = expandedArgs;
    }

    @Override
    public String toString() {
        return "Arg " + idx + " <" + typeRawClass + "> " +
                (sqlPart == null ? "" : "[" + sqlPart + "] ") +
                (varArgInfo == null ? "" : " var arg element type " + varArgInfo.clazz + " with glue '" + varArgInfo.methodName + "'") +
                (expandedArgs == null || expandedArgs.length == 0 ? "" : " expanded as " + Arrays.asList(expandedArgs));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arg that = (Arg) o;
        return Objects.equals(typeRawClass, that.typeRawClass) &&
                Objects.equals(sqlPart, that.sqlPart) &&
                Objects.equals(idx, that.idx) &&
                Objects.equals(varArgInfo, that.varArgInfo) &&
                Arrays.equals(expandedArgs, that.expandedArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeRawClass, sqlPart, idx, varArgInfo, expandedArgs);
    }

    public boolean isDynamic() {
        return sqlPart != null && idx.optional || varArgInfo != null;
    }
}
