package org.xblackcat.sjpu.storage.converter.builder;

/**
 * 20.10.2015 16:55
 *
 * @author xBlackCat
 */
public class AnIdObject<O> {
    private final O id;

    public AnIdObject(O id) {
        this.id = id;
    }

    public O getId() {
        return id;
    }
}
