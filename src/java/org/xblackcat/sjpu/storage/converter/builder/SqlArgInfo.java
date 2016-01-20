package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Arrays;
import java.util.Objects;

/**
 * 13.01.2016 11:14
 *
 * @author xBlackCat
 */
public final class SqlArgInfo {
    public final String sqlPart;
    public final ArgInfo varArgInfo;
    public final ArgIdx argIdx;
    public final ArgInfo[] expandingType;

    public SqlArgInfo(String sqlPart, int argIdx, boolean optional, ArgInfo... expandingType) {
        this(sqlPart, null, argIdx, optional, expandingType);
    }

    public SqlArgInfo(
            String sqlPart,
            ArgInfo varArgInfo,
            int argIdx,
            boolean optional,
            ArgInfo... expandingType
    ) {
        this.varArgInfo = varArgInfo;
        this.expandingType = expandingType;
        this.argIdx = new ArgIdx(argIdx, optional);
        this.sqlPart = sqlPart;
    }

    @Override
    public String toString() {
        return "SqlArgInfo " + argIdx + " [" + sqlPart + "]" +
                (varArgInfo == null ? "" : " var arg element type " + varArgInfo.clazz + " with glue '" + varArgInfo.methodName + "'") +
                (expandingType == null || expandingType.length == 0 ? "" : " expanded as " + Arrays.asList(expandingType));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlArgInfo that = (SqlArgInfo) o;
        return Objects.equals(sqlPart, that.sqlPart) &&
                Objects.equals(varArgInfo, that.varArgInfo) &&
                Objects.equals(argIdx, that.argIdx) &&
                Arrays.equals(expandingType, that.expandingType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlPart, varArgInfo, argIdx, expandingType);
    }
}
