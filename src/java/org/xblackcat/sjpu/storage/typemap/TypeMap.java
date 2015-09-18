package org.xblackcat.sjpu.storage.typemap;

import java.sql.Connection;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Simple type map implementation: use external functions for converting user type to/from db type. Null values are passed to converting
 * functions as well as other values. To avoid passing null values to converting functions use {@linkplain NullPassTypeMap} class.
 *
 * @author xBlackCat
 */
public class TypeMap<RealObject, DBObject> extends ATypeMap<RealObject, DBObject> {
    private final BiFunction<Connection, RealObject, DBObject> forStore;
    private final Function<DBObject, RealObject> forRead;

    public TypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            Function<RealObject, DBObject> forStore,
            Function<DBObject, RealObject> forRead
    ) {
        this(realType, dbType, (c, realObject) -> forStore.apply(realObject), forRead);
    }

    public TypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            BiFunction<Connection, RealObject, DBObject> forStore,
            Function<DBObject, RealObject> forRead
    ) {
        super(realType, dbType);
        this.forStore = forStore;
        this.forRead = forRead;
    }

    @Override
    public DBObject forStore(Connection con, RealObject obj) {
        return forStore.apply(con, obj);
    }

    @Override
    public RealObject forRead(DBObject obj) {
        return forRead.apply(obj);
    }
}
