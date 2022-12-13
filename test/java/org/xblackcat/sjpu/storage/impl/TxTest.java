package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;

/**
 * 18.11.13 12:48
 *
 * @author xBlackCat
 */
public class TxTest {
    private Storage storage;

    @BeforeEach
    public void setupDatabase() throws StorageException {
        IConnectionFactory helper = StorageUtils.buildConnectionFactory(Config.TEST_DB_CONFIG);
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
            Assertions.assertEquals("Text-" + i, dbah.get(i), "Index [" + i + "]");
        }

        // Rollback
        try (ITx b = storage.beginTransaction()) {
            DBAH tx = b.get(DBAH.class);
            tx.fill(100, "Checkpoint1");
            tx.fill(101, "Checkpoint2");
            tx.fill(102, "Checkpoint3");
        }
        Assertions.assertNull(dbah.get(100));
        Assertions.assertNull(dbah.get(101));
        Assertions.assertNull(dbah.get(102));

        try (ITx b = storage.beginTransaction()) {
            DBAH tx = b.get(DBAH.class);
            tx.fill(100, "Checkpoint1");
            tx.fill(101, "Checkpoint2");
            tx.fill(102, "Checkpoint3");

            b.commit();
        }
        Assertions.assertEquals("Checkpoint1", dbah.get(100));
        Assertions.assertEquals("Checkpoint2", dbah.get(101));
        Assertions.assertEquals("Checkpoint3", dbah.get(102));

    }

    @Test
    public void checkDataBatch() throws StorageException {
        DBAH dbah = storage.get(DBAH.class);
        dbah.createDB();
        for (int i = 0; i < 10; i++) {
            dbah.fill(i, "Text-" + i);
        }

        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals("Text-" + i, dbah.get(i), "Index [" + i + "]");
        }

        // Rollback
        try (ITx b = storage.beginTransaction()) {
            try (DBBAH tx = b.startBatch(DBBAH.class)) {
                tx.fill(100, "Checkpoint1");
                tx.fill(101, "Checkpoint2");
                tx.fill(102, "Checkpoint3");
            }
        }
        Assertions.assertNull(dbah.get(100));
        Assertions.assertNull(dbah.get(101));
        Assertions.assertNull(dbah.get(102));

        try (ITx b = storage.beginTransaction()) {
            try (DBBAH tx = b.startBatch(DBBAH.class)) {
                tx.fill(100, "Checkpoint1");
                tx.fill(101, "Checkpoint2");
                tx.fill(102, "Checkpoint3");
            }
            b.commit();
        }
        Assertions.assertEquals("Checkpoint1", dbah.get(100));
        Assertions.assertEquals("Checkpoint2", dbah.get(101));
        Assertions.assertEquals("Checkpoint3", dbah.get(102));

        try (ITx b = storage.beginTransaction()) {
            try (DBBAH tx = b.startBatch(DBBAH.class)) {
                tx.fill(103, "Checkpoint4");
                tx.fill(104, "Checkpoint5");
                tx.fill(105, "Checkpoint6");
            }
            try (DBBAH tx = b.startBatch(DBBAH.class)) {
                tx.fill(106, "Checkpoint7");
                tx.fill(107, "Checkpoint8");
                tx.fill(108, "Checkpoint9");
            }
            b.commit();
        }
        Assertions.assertEquals("Checkpoint4", dbah.get(103));
        Assertions.assertEquals("Checkpoint5", dbah.get(104));
        Assertions.assertEquals("Checkpoint6", dbah.get(105));
        Assertions.assertEquals("Checkpoint7", dbah.get(106));
        Assertions.assertEquals("Checkpoint8", dbah.get(107));
        Assertions.assertEquals("Checkpoint9", dbah.get(108));

    }

    @Test
    public void returnKeysSet() throws StorageException {
        final DBAutoIncAH dbAH = storage.get(DBAutoIncAH.class);

        dbAH.createDB();

        {
            final String value = "Hello, H2 database :)";
            int id = dbAH.put(value);

            Assertions.assertEquals(value, dbAH.get(id));
        }

        {
            final String value = "Hello again, H2 database :)";
            final MutableInt key = new MutableInt();

            dbAH.put(
                    o -> {
                        key.setValue(o);
                        return false;
                    },
                    value
            );

            Assertions.assertEquals(value, dbAH.get(key.intValue()));
        }
    }

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

    /**
     * 18.11.13 12:53
     *
     * @author xBlackCat
     */
    public interface DBBAH extends IBatchedAH {
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
    public interface DBAutoIncAH extends IAH {
        @Sql("CREATE TABLE \"autoinc\" (\"id\" INT PRIMARY KEY AUTO_INCREMENT NOT NULL, \"txt\" VARCHAR NOT NULL)")
        void createDB() throws StorageException;

        @Sql("INSERT INTO \"autoinc\" (\"txt\") VALUES (?)")
        int put(String value) throws StorageException;

        @Sql("INSERT INTO \"autoinc\" (\"txt\") VALUES (?)")
        @MapRowTo(Integer.class)
        void put(IRowConsumer<Integer> id, String value) throws StorageException;

        @Sql("SELECT\n" +
                "  \"txt\"\n" +
                "FROM \"autoinc\"\n" +
                "WHERE \"id\" = ?")
        String get(int id) throws StorageException;
    }
}
