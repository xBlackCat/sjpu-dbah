package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.StorageSetupException;

/**
 * 04.04.2014 15:40
 *
 * @author xBlackCat
 */
public interface IBuilder<P, B> {
    <T extends B> T build(Class<T> target, P helper) throws StorageSetupException;
}
