package org.xblackcat.sjpu.storage.workflow.base;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.DDL;

/**
 * 22.04.2014 18:09
 *
 * @author xBlackCat
 */
public interface IDBInitAH extends IAH {
    @DDL({"CREATE TABLE list (id INT, name TEXT, PRIMARY KEY (id))", "CREATE TABLE uri (id INT AUTO_INCREMENT, uri TEXT, PRIMARY KEY (id))"})
    void createDB() throws StorageException;
}
