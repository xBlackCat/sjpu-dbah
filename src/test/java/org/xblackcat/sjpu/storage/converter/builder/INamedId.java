package org.xblackcat.sjpu.storage.converter.builder;

/**
 * 21.10.2015 8:54
 *
 * @author xBlackCat
 */
public interface INamedId<N extends Number> {
    N getId();

    String getName();
}
