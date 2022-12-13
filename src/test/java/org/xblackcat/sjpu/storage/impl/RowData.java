package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.ann.DefaultRowMap;

/**
 * 11.12.13 13:48
 *
 * @author xBlackCat
 */
public class RowData {
    private final int id;
    private final String name;

    public RowData(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @DefaultRowMap
    public RowData(String id, String name) {
        this(Integer.valueOf(id), name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
