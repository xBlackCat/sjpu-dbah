package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Objects;

/**
 * 13.01.2016 11:15
 *
 * @author xBlackCat
 */
public class ArgIdx {
    public final int idx;
    public final boolean optional;

    public ArgIdx(int idx) {
        this(idx, false);
    }

    public ArgIdx(int idx, boolean optional) {
        this.idx = idx;
        this.optional = optional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgIdx argIdx = (ArgIdx) o;
        return idx == argIdx.idx &&
                optional == argIdx.optional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx, optional);
    }

    @Override
    public String toString() {
        return "#" + idx + (optional ? " (O)" : " (P)");
    }
}
