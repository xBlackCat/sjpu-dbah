package org.xblackcat.sjpu.storage.consumer;

/**
 * 16.02.14 14:55
 *
 * @author xBlackCat
 */
public interface IRowSetConsumer<RowsHolder, Object> extends IRowConsumer<Object> {
    RowsHolder getRowsHolder();
}
