package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;

/**
 * @author xBlackCat Date: 27.07.11
 */
public class AnAH implements IAH {
    protected final AQueryHelper helper;

    protected AnAH(AQueryHelper helper) {
        if (helper == null) {
            throw new NullPointerException("Helper can not be null.");
        }
        this.helper = helper;
    }
}
