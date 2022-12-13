package org.xblackcat.sjpu.storage.connection;

public record DBConfig(String driver, String url, String user, String password, int poolSize) implements IDBConfig {
}
