package org.xblackcat.sjpu.storage.typemap;

/**
 * To use Type map feature a ATypeMap implementation should have a default constructor which passes correct class objects to super
 * constructor
 * <p>
 * 17.12.13 14:31
 *
 * @author xBlackCat
 */
public abstract class ATypeMap<RealObject, DBObject> implements ITypeMap<RealObject, DBObject> {
    private final Class<RealObject> realType;
    private final Class<DBObject> dbType;

    protected ATypeMap(Class<RealObject> realType, Class<DBObject> dbType) {
        this.realType = realType;
        this.dbType = dbType;
    }

    @Override
    public Class<RealObject> getRealType() {
        return realType;
    }

    @Override
    public Class<DBObject> getDbType() {
        return dbType;
    }

}
