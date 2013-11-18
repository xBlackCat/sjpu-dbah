package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 18.11.13 12:53
 *
 * @author xBlackCat
 */
public interface DBAH extends IAH {
    @Sql("CREATE TABLE \"data\" (\"id\" INT PRIMARY KEY NOT NULL, \"txt\" VARCHAR NOT NULL)")
    void createDB() throws StorageException;

    @Sql("INSERT INTO \"data\" (\"id\", \"txt\") VALUES (?, ?)")
    void fill(int id, String txt) throws StorageException;

    @Sql("SELECT\n" +
                 "  \"txt\"\n" +
                 "FROM \"data\"\n" +
                 "WHERE \"id\" = ?")
    String get(int id) throws StorageException;
}
