package org.xblackcat.sjpu.storage.connection;

/**
 * 14.04.2014 17:47
 *
 * @author xBlackCat
 */
public class DBConfig implements IDBConfig {
    private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final int poolSize;

    public DBConfig(String driver, String url, String user, String password, int poolSize) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.poolSize = poolSize;
    }

    @Override
    public String getDriver() {
        return driver;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }
}
