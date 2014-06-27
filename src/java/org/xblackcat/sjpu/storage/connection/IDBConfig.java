package org.xblackcat.sjpu.storage.connection;

/**
 * 12.02.13 12:24
 *
 * @author xBlackCat
 */
public interface IDBConfig {
    String getDriver();

    String getUrl();

    String getUser();

    String getPassword();

    int getPoolSize();
}
