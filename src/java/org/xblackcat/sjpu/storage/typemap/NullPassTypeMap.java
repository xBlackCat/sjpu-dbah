package org.xblackcat.sjpu.storage.typemap;

import org.xblackcat.sjpu.util.function.BiFunctionEx;
import org.xblackcat.sjpu.util.function.FunctionEx;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base type mapper for avoiding pass null value to converter functions. Null value will be remain null value and
 * To handle null values by yourself use {@linkplain TypeMap} implementation.
 *
 * @author xBlackCat
 */
public class NullPassTypeMap<RealObject, DBObject> extends TypeMap<RealObject, DBObject> {
    public NullPassTypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            FunctionEx<RealObject, DBObject, SQLException> forStore,
            FunctionEx<DBObject, RealObject, SQLException> forRead
    ) {
        super(realType, dbType, forStore, forRead);
    }

    public NullPassTypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            BiFunctionEx<Connection, RealObject, DBObject, SQLException> forStore,
            FunctionEx<DBObject, RealObject, SQLException> forRead
    ) {
        super(realType, dbType, forStore, forRead);
    }

    @Override
    public DBObject forStore(Connection con, RealObject obj) throws SQLException {
        if (obj == null) {
            return null;
        }

        return super.forStore(con, obj);
    }

    @Override
    public RealObject forRead(DBObject obj) throws SQLException {
        if (obj == null) {
            return null;
        }

        return super.forRead(obj);
    }
}
