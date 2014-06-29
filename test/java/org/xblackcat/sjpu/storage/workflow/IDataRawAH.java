package org.xblackcat.sjpu.storage.workflow;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.ann.SqlPart;
import org.xblackcat.sjpu.storage.consumer.IRawProcessor;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public interface IDataRawAH extends IAH {
    @Sql("INSERT INTO list (id, name) VALUES (?, ?)")
    void put(int id, Numbers element) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n")
    void getListElement(IRawProcessor consumer) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM {0}\n")
    void getListElement(IRawProcessor consumer, @SqlPart(0) String tableName) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM {0}\n")
    void getListElement(@SqlPart(0) String tableName, IRawProcessor consumer) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM {0}\n" +
                 "WHERE\n" +
                 "  id >= ?")
    void getListElement(IRawProcessor consumer, @SqlPart(0) String tableName, int ind) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM {0}\n" +
                 "WHERE\n" +
                 "  id >= ?")
    void getListElement(@SqlPart(0) String tableName, IRawProcessor consumer, int ind) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM {0}\n" +
                 "WHERE\n" +
                 "  id >= ?")
    void getListElement(IRawProcessor consumer, int ind, @SqlPart(0) String tableName) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n" +
                 "WHERE\n" +
                 "  id >= ?")
    void getListElement(IRawProcessor consumer, int ind) throws StorageException;

    @Sql("DELETE FROM list")
    void dropElements() throws StorageException;
}
