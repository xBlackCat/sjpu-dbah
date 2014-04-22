package org.xblackcat.sjpu.storage.workflow;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
* 22.04.2014 18:09
*
* @author xBlackCat
*/
public interface IDBInitAH extends IAH {
    @Sql("CREATE TABLE list (id INT, name TEXT, PRIMARY KEY (id))")
    void init1() throws StorageException;

    @Sql("CREATE TABLE uri (id INT AUTO_INCREMENT, uri TEXT, PRIMARY KEY (id))")
    void init2() throws StorageException;
}
