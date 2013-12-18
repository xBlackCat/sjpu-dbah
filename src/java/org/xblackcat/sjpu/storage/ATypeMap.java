package org.xblackcat.sjpu.storage;

/**
 * To use Type map feature a ATypeMap implementation should have a default constructor which passes correct class objects to super
 * constructor (see Unit test for example: {@linkplain org.xblackcat.sjpu.storage.impl.TypeMapperTest})
 *
 * 17.12.13 14:31
 *
 * @author xBlackCat
 */
public abstract class ATypeMap<RealObject, DBObject> {
    private final Class<RealObject> realType;
    private final Class<DBObject> dbType;

    protected ATypeMap(Class<RealObject> realType, Class<DBObject> dbType) {
        this.realType = realType;
        this.dbType = dbType;
    }

    public Class<RealObject> getRealType() {
        return realType;
    }

    public Class<DBObject> getDbType() {
        return dbType;
    }

    public abstract DBObject forStore(RealObject obj);

    public abstract RealObject forRead(DBObject obj);
}
