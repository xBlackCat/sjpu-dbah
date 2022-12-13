package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.ann.ExtractFields;

/**
 * 14.10.2015 14:15
 *
 * @author xBlackCat
 */
@ExtractFields({"id", "name"})
public class AutoNamedItem {
    private final int id;
    private final String name;

    public AutoNamedItem(int id, String name) {
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
