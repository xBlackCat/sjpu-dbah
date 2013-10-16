package org.xblackcat.sjpu.storage.connection;

/**
 * 12.02.13 12:24
 *
 * @author xBlackCat
 */
public interface IDatabaseSettings {
    String getDbJdbcDriverClass();

    String getDbConnectionUrlPattern();

    String getDbAccessUser();

    String getDbAccessPassword();

    int getDbPoolSize();
}
