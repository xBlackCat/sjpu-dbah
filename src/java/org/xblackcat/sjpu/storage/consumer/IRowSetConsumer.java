package org.xblackcat.sjpu.storage.consumer;

/**
 * Consume all results to a container and return the container. The implementation should have a default constructor or constructor which
 * takes one parameter: target object class
 * <p/>
 * Implementation of the interface could be registered at storage setup procedure.
 *
 * @author xBlackCat
 * @see org.xblackcat.sjpu.storage.consumer.ToEnumSetConsumer
 * @see org.xblackcat.sjpu.storage.StorageBuilder#addRowSetConsumer(Class, Class)
 */
public interface IRowSetConsumer<RowsHolder, Object> extends IRowConsumer<Object> {
    RowsHolder getRowsHolder();
}
