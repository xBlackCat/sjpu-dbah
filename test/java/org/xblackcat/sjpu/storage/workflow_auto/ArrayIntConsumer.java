package org.xblackcat.sjpu.storage.workflow_auto;

import org.xblackcat.sjpu.storage.ConsumeException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * 22.04.2014 18:10
 *
 * @author xBlackCat
 */
@MapRowTo(Integer.class)
public class ArrayIntConsumer implements IRowSetConsumer<int[], Integer> {
    private final List<Integer> list = new ArrayList<>();

    @Override
    public int[] getRowsHolder() {
        final int[] ints = new int[list.size()];
        int i = 0;
        for (Integer v : list) {
            ints[i++] = v;
        }
        return ints;
    }

    @Override
    public boolean consume(Integer o) throws ConsumeException {
        if (o == null) {
            throw new ConsumeException("Can't consume null value");
        }
        list.add(o);
        return false;
    }
}
