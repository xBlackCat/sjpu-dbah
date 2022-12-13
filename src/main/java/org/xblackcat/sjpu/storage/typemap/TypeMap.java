package org.xblackcat.sjpu.storage.typemap;

import org.xblackcat.sjpu.util.function.BiFunctionEx;
import org.xblackcat.sjpu.util.function.FunctionEx;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple type map implementation: use external functions for converting user type to/from db type. Null values are passed to converting
 * functions as well as other values. To avoid passing null values to converting functions use {@linkplain NullPassTypeMap} class.
 *
 * @author xBlackCat
 */
public class TypeMap<RealObject, DBObject> extends ATypeMap<RealObject, DBObject> {
    private final BiFunctionEx<Connection, RealObject, DBObject, SQLException> forStore;
    private final FunctionEx<DBObject, RealObject, SQLException> forRead;

    public TypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            FunctionEx<RealObject, DBObject, SQLException> forStore,
            FunctionEx<DBObject, RealObject, SQLException> forRead
    ) {
        this(realType, dbType, (c, realObject) -> forStore.apply(realObject), forRead);
    }

    public TypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            BiFunctionEx<Connection, RealObject, DBObject, SQLException> forStore,
            FunctionEx<DBObject, RealObject, SQLException> forRead
    ) {
        super(realType, dbType);
        this.forStore = forStore;
        this.forRead = forRead;
    }

    @Override
    public DBObject forStore(Connection con, RealObject obj) throws SQLException {
        return forStore.apply(con, obj);
    }

    @Override
    public RealObject forRead(DBObject obj) throws SQLException {
        return forRead.apply(obj);
    }
}
