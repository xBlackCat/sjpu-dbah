package org.xblackcat.sjpu.storage.impl;

import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xblackcat.sjpu.storage.IBatch;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.StorageUtils;
import org.xblackcat.sjpu.storage.connection.IDatabaseSettings;

/**
 * 18.11.13 12:48
 *
 * @author xBlackCat
 */
public class BatchTest {
    private static IDatabaseSettings settings = new IDatabaseSettings() {
        @Override
        public String getDbJdbcDriverClass() {
            return Driver.class.getName();
        }

        @Override
        public String getDbConnectionUrlPattern() {
            return "jdbc:h2:mem:db1";
        }

        @Override
        public String getDbAccessUser() {
            return null;
        }

        @Override
        public String getDbAccessPassword() {
            return null;
        }

        @Override
        public int getDbPoolSize() {
            return 10;
        }
    };
    private Storage storage;

    @Before
    public void setupDatabase() throws StorageException {
        AQueryHelper helper = StorageUtils.buildQueryHelper(settings);
        storage = new Storage(helper);

        DBAH dbah = storage.get(DBAH.class);
        dbah.createDB();
        for (int i = 0; i < 10; i++) {
            dbah.fill(i, "Text-" + i);
        }
    }

    @Test
    public void checkData() throws Exception {
        DBAH dbah = storage.get(DBAH.class);
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
    public void returnKeysSet() throws Exception {
        DBAutoIncAH dbAH = storage.get(DBAutoIncAH.class);

        dbAH.createDB();

        final String value = "Hello, H2 database :)";
        int id = dbAH.put(value);

        Assert.assertEquals(value, dbAH.get(id));
    }
}
