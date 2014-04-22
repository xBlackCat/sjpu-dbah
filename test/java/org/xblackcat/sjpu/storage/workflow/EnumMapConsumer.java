package org.xblackcat.sjpu.storage.workflow;

import org.xblackcat.sjpu.storage.ConsumeException;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;

import java.util.EnumMap;
import java.util.Map;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public class EnumMapConsumer implements IRowSetConsumer<Map<Numbers, Integer>, IElement<Numbers>> {
    private final Map<Numbers, Integer> map = new EnumMap<>(Numbers.class);

    @Override
    public Map<Numbers, Integer> getRowsHolder() {
        return map;
    }

    @Override
    public boolean consume(IElement<Numbers> o) throws ConsumeException {
        map.put(o.getName(), o.getId());
        return false;
    }
}
