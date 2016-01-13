package org.xblackcat.sjpu.storage.converter.builder;

import java.util.Objects;

/**
 * 13.01.2016 11:14
 *
 * @author xBlackCat
 */
public final class SqlArgInfo {
    public final String sqlPart;
    public final ArgIdx argIdx;

    public SqlArgInfo(String sqlPart, int argIdx, boolean optional) {
        this.argIdx = new ArgIdx(argIdx, optional);
        this.sqlPart = sqlPart;
    }

    @Override
    public String toString() {
        return "SqlArgInfo " + argIdx + " [" + sqlPart + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlArgInfo that = (SqlArgInfo) o;
        return Objects.equals(sqlPart, that.sqlPart) &&
                Objects.equals(argIdx, that.argIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlPart, argIdx);
    }
}
