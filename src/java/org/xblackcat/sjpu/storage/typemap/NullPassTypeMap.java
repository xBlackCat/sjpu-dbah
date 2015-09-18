package org.xblackcat.sjpu.storage.typemap;

import java.sql.Connection;
import java.util.function.BiFunction;
import java.util.function.Function;

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
            Function<RealObject, DBObject> forStore,
            Function<DBObject, RealObject> forRead
    ) {
        super(realType, dbType, forStore, forRead);
    }

    public NullPassTypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            BiFunction<Connection, RealObject, DBObject> forStore,
            Function<DBObject, RealObject> forRead
    ) {
        super(realType, dbType, forStore, forRead);
    }

    @Override
    public DBObject forStore(Connection con, RealObject obj) {
        if (obj == null) {
            return null;
        }

        return super.forStore(con, obj);
    }

    @Override
    public RealObject forRead(DBObject obj) {
        if (obj == null) {
            return null;
        }

        return super.forRead(obj);
    }
}
