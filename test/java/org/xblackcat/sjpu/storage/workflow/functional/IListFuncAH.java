package org.xblackcat.sjpu.storage.workflow.functional;

import org.xblackcat.sjpu.storage.IFunctionalAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.SqlType;
import org.xblackcat.sjpu.storage.workflow.data.Element;

import java.util.List;

/**
 * 24.04.2015 17:48
 *
 * @author xBlackCat
 */
public interface IListFuncAH extends IFunctionalAH {
    @SqlType(QueryType.Select)
    List<Element> getList() throws StorageException;
}
