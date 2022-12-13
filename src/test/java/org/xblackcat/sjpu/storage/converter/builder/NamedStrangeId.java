package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.ann.ExtractFields;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 20.10.2015 16:56
 *
 * @author xBlackCat
 */
@ExtractFields({"id", "name"})
public class NamedStrangeId extends AnIdObject<Function<Callable<String>, List<Double>>> {
    private final String name;

    public NamedStrangeId(Function<Callable<String>, List<Double>> id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
