package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.ann.Sql;

/**
 * 18.11.13 12:48
 *
 * @author xBlackCat
 */
public class BatchTest {
    private Storage storage;

    @Before
    public void setupDatabase() throws StorageException {
        IQueryHelper helper = StorageUtils.buildQueryHelper(Config.TEST_DB_CONFIG);
        storage = new Storage(helper);
    }

    @Test
    public void checkData() throws StorageException {
        DBAH dbah = storage.get(DBAH.class);
        dbah.createDB();
        for (int i = 0; i < 10; i++) {
            dbah.fill(i, "Text-" + i);
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("Index [" + i + "]", "Text-" + i, dbah.get(i));
        }

        // Rollback
        try (IBatch b = storage.openTransaction()) {
            DBAH tx = b.get(DBAH.class);
            tx.fill(100, "Checkpoint1");
            tx.fill(101, "Checkpoint2");
            tx.fill(102, "Checkpoint3");
        }
        Assert.assertNull(dbah.get(100));
        Assert.assertNull(dbah.get(101));
        Assert.assertNull(dbah.get(102));

        try (IBatch b = storage.openTransaction()) {
            DBAH tx = b.get(DBAH.class);
            tx.fill(100, "Checkpoint1");
            tx.fill(101, "Checkpoint2");
            tx.fill(102, "Checkpoint3");

            b.commit();
        }
        Assert.assertEquals("Checkpoint1", dbah.get(100));
        Assert.assertEquals("Checkpoint2", dbah.get(101));
        Assert.assertEquals("Checkpoint3", dbah.get(102));

    }

    @Test
    public void returnKeysSet() throws StorageException {
        DBAutoIncAH dbAH = storage.get(DBAutoIncAH.class);

        dbAH.createDB();

        final String value = "Hello, H2 database :)";
        int id = dbAH.put(value);

        Assert.assertEquals(value, dbAH.get(id));
    }

    /**
     * 18.11.13 12:53
     *
     * @author xBlackCat
     */
    public static interface DBAH extends IAH {
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

    /**
     * 18.11.13 12:53
     *
     * @author xBlackCat
     */
    public static interface DBAutoIncAH extends IAH {
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
}
