package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IFunctionalAH;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

public class AFunctionalAH extends AnAH implements IFunctionalAH {
    private final String sql;

    protected AFunctionalAH(IConnectionFactory factory, String sql) {
        super(factory);
        if (sql == null) {
            throw new NullPointerException("Sql can't be null");
        }
        this.sql = sql;
    }

    protected String getSql() {
        return sql;
    }
}
