package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xblackcat.sjpu.storage.Config;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.StorageUtils;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

/**
 * 13.12.13 16:36
 *
 * @author xBlackCat
 */
public class MultiLevelHelpersTest {
    private Storage storage;

    @Before
    public void setupDatabase() throws StorageException {
        IConnectionFactory helper = StorageUtils.buildQueryHelper(Config.TEST_DB_CONFIG);
        storage = new Storage(helper);
    }

    @Test
    public void testLevel1() {
        final Level1AH ah = storage.get(Level1AH.class);

        Assert.assertEquals(2, ah.getClass().getDeclaredMethods().length);
    }

    @Test
    public void testLevel2() {
        final Level2AH ah = storage.get(Level2AH.class);

        Assert.assertEquals(3, ah.getClass().getDeclaredMethods().length);
    }

    @Test
    public void testLevel3() {
        final Level3AH ah = storage.get(Level3AH.class);

        Assert.assertEquals(4, ah.getClass().getDeclaredMethods().length);
    }

    @Test
    public void testLevel4() {
        final Level4AH ah = storage.get(Level4AH.class);

        Assert.assertEquals(2, ah.getClass().getDeclaredMethods().length);
    }

}
