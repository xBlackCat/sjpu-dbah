package org.xblackcat.sjpu.storage.typemap;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 19.12.13 16:42
 *
 * @author xBlackCat
 */
public interface ITypeMap<RealObject, DBObject> {
    Class<RealObject> getRealType();

    Class<DBObject> getDbType();

    DBObject forStore(Connection con, RealObject obj) throws SQLException;

    RealObject forRead(DBObject obj) throws SQLException;
}
