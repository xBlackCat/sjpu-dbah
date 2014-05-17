package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

/**
 * @author xBlackCat Date: 27.07.11
 */
public class AnAH implements IAH {
    protected final IConnectionFactory factory;

    protected AnAH(IConnectionFactory factory) {
        if (factory == null) {
            throw new NullPointerException("Helper can not be null.");
        }
        this.factory = factory;
    }
}
