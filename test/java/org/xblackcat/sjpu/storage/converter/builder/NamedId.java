package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.ann.ExtractFields;

/**
 * 20.10.2015 16:56
 *
 * @author xBlackCat
 */
@ExtractFields({"id", "name"})
public class NamedId extends AnIdObject<Integer> implements INamedId<Integer> {
    private final String name;

    public NamedId(Integer id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
