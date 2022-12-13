package org.xblackcat.sjpu.storage;

import org.h2.Driver;
import org.xblackcat.sjpu.storage.connection.DBConfig;

/**
 * 14.04.2014 17:52
 *
 * @author xBlackCat
 */
public class Config {
    public static final DBConfig TEST_DB_CONFIG = new DBConfig(Driver.class.getName(), "jdbc:h2:mem:", null, null, 10);
}
