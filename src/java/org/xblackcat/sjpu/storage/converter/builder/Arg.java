package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Objects;

/**
 * 13.01.2016 11:14
 *
 * @author xBlackCat
 */
public final class Arg {
    public final ArgInfo info;
    public final ArgIdx idx;

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
        this.idx = new ArgIdx(idx, optional);
        info = new ArgInfo(clazz, methodName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arg arg = (Arg) o;
        return Objects.equals(info, arg.info) &&
                Objects.equals(idx, arg.idx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info, idx);
    }

    @Override
    public String toString() {
        return idx + " <" + info + ">";
    }
}
