package org.xblackcat.sjpu.storage.converter.builder;

public record ArgInfo(Class<?> clazz, String methodName) {
    public static final ArgInfo[] NO_ARG_INFOS = new ArgInfo[0];

    @Override
    public String toString() {
        return (clazz != null ? clazz.getName() : "<to resolve>") + (methodName == null ? "" : " from field " + methodName);
    }
}
