package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Objects;

/**
 * 13.01.2016 11:14
 *
 * @author xBlackCat
 */
public final class Arg {
    public final Class<?> clazz;
    public final String methodName;
    public final ArgIdx argIdx;

    public Arg(Class<?> clazz, int idx) {
        this(clazz, idx, null, false);
    }

    public Arg(Class<?> clazz, int idx, String methodName) {
        this(clazz, idx, methodName, false);
    }

    public Arg(Class<?> clazz, int idx, boolean optional) {
        this(clazz, idx, null, optional);
    }

    public Arg(Class<?> clazz, int idx, String methodName, boolean optional) {
        argIdx = new ArgIdx(idx, optional);
        this.clazz = clazz;
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arg arg = (Arg) o;
        return Objects.equals(clazz, arg.clazz) &&
                Objects.equals(methodName, arg.methodName) &&
                Objects.equals(argIdx, arg.argIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, methodName, argIdx);
    }
}
