package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Objects;

/**
 * 18.01.2016 11:05
 *
 * @author xBlackCat
 */
public class ArgInfo {
    public static final ArgInfo[] NO_ARG_INFOS = new ArgInfo[0];
    public final Class<?> clazz;
    public final String methodName;

    public ArgInfo(Class<?> clazz, String methodName) {
        this.clazz = clazz;
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgInfo argInfo = (ArgInfo) o;
        return Objects.equals(clazz, argInfo.clazz) &&
                Objects.equals(methodName, argInfo.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, methodName);
    }

    @Override
    public String toString() {
        return (clazz != null ? clazz.getName() : "<to resolve>") + (methodName == null ? "" : " from field " + methodName);
    }
}
