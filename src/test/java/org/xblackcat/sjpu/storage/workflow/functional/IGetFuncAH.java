package org.xblackcat.sjpu.storage.workflow.functional;

import org.xblackcat.sjpu.storage.IFunctionalAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.SqlType;
import org.xblackcat.sjpu.storage.workflow.data.ElementNumber;

/**
 * 24.04.2015 17:48
 *
 * @author xBlackCat
 */
public interface IGetFuncAH extends IFunctionalAH {
    @SqlType(QueryType.Select)
    ElementNumber getElement(int id) throws StorageException;
}
