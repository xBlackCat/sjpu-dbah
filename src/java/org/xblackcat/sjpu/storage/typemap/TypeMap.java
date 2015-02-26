package org.xblackcat.sjpu.storage.typemap;

import java.util.function.Function;

/**
 * Simple type map implementation: use external functions for converting user type to/from db type. Null values are passed to converting
 * functions as well as other values. To avoid passing null values to converting functions use {@linkplain NullPassTypeMap} class.
 *
 * @author xBlackCat
 */
public class TypeMap<RealObject, DBObject> extends ATypeMap<RealObject, DBObject> {
    private final Function<RealObject, DBObject> forStore;
    private final Function<DBObject, RealObject> forRead;

    protected TypeMap(
            Class<RealObject> realType,
            Class<DBObject> dbType,
            Function<RealObject, DBObject> forStore,
            Function<DBObject, RealObject> forRead
    ) {
        super(realType, dbType);
        this.forStore = forStore;
        this.forRead = forRead;
    }

    @Override
    public DBObject forStore(RealObject obj) {
        return forStore.apply(obj);
    }

    @Override
    public RealObject forRead(DBObject obj) {
        return forRead.apply(obj);
    }
}
