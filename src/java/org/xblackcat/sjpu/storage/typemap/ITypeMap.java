package org.xblackcat.sjpu.storage.typemap;

import java.sql.Connection;

/**
 * 19.12.13 16:42
 *
 * @author xBlackCat
 */
public interface ITypeMap<RealObject, DBObject> {
    Class<RealObject> getRealType();

    Class<DBObject> getDbType();

    DBObject forStore(Connection con, RealObject obj);

    RealObject forRead(DBObject obj);
}
