package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageSetupException;

/**
 * 21.02.13 11:46
 *
 * @author xBlackCat
 */
interface IAHBuilder<P> {
    <T extends IAH> T build(Class<T> target, P helper) throws StorageSetupException;
}
