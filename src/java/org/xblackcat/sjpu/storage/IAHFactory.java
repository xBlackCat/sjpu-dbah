package org.xblackcat.sjpu.storage;

/**
 * 15.11.13 14:17
 *
 * @author xBlackCat
 */
public interface IAHFactory {
    <I extends IAH> I get(Class<I> clazz);

    <T extends IFunctionalAH> T get(Class<T> functionalAH, String sql);

    <T extends IBatchedAH> T startBatch(Class<T> batched);

//    <T extends IFunctionalAH & IBatchedAH> T startBatch(Class<T> functionalAH, String sql);
}
