package org.xblackcat.sjpu.storage.connection;

/**
 * 12.02.13 12:24
 *
 * @author xBlackCat
 */
public interface IDBConfig {
    String driver();

    String url();

    String user();

    String password();

    int poolSize();
}
