package org.xblackcat.sjpu.storage.converter.builder;

/**
 * 14.10.2015 14:15
 *
 * @author xBlackCat
 */
public class NamedItem {
    private final int id;
    private final String name;

    public NamedItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }


}
