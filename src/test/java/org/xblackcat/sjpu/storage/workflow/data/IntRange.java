package org.xblackcat.sjpu.storage.workflow.data;

import org.xblackcat.sjpu.storage.ann.ExtractFields;

/**
 * 18.01.2016 11:51
 *
 * @author xBlackCat
 */
@ExtractFields({"minimum", "maximum"})
public class IntRange {
    private final int minimum;
    private final int maximum;

    public IntRange(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }
}
