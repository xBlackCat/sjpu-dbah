package org.xblackcat.sjpu.storage.converter.builder;

public record ArgIdx(int idx, boolean optional) {
    public ArgIdx(int idx) {
        this(idx, false);
    }

    @Override
    public String toString() {
        return "#" + idx + (optional ? " (O)" : " (P)");
    }
}
