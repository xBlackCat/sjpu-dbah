package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.Sql;
import org.xblackcat.sjpu.storage.StorageException;

/**
 * 18.11.13 12:53
 *
 * @author xBlackCat
 */
public interface DBAutoIncAH extends IAH {
    @Sql("CREATE TABLE \"autoinc\" (\"id\" INT PRIMARY KEY AUTO_INCREMENT NOT NULL, \"txt\" VARCHAR NOT NULL)")
    void createDB() throws StorageException;

    @Sql("INSERT INTO \"autoinc\" (\"txt\") VALUES (?)")
    int put(String value) throws StorageException;

    @Sql("SELECT\n" +
                 "  \"txt\"\n" +
                 "FROM \"autoinc\"\n" +
                 "WHERE \"id\" = ?")
    String get(int id) throws StorageException;
}
