package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IFunctionalAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.SqlType;

/**
 * 16.08.13 15:55
 *
 * @author xBlackCat
 */
public interface IPrimitiveFunctionalAH extends IFunctionalAH {
    @SqlType(QueryType.Select)
    int getIntBySqlAnn() throws StorageException;
}
