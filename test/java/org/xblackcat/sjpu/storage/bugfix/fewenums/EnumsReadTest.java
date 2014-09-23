package org.xblackcat.sjpu.storage.bugfix.fewenums;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;
import org.xblackcat.sjpu.storage.workflow.base.IDBInitAH;
import org.xblackcat.sjpu.storage.workflow.data.ArrayIntConsumer;

import java.util.List;

/**
 * 23.09.2014 11:24
 *
 * @author xBlackCat
 */
public class EnumsReadTest {
    private IStorage storage;

    @Before
    public void setupDatabase() throws StorageException {
        IConnectionFactory helper = StorageUtils.buildConnectionFactory(Config.TEST_DB_CONFIG);
        final StorageBuilder builder = new StorageBuilder();
        builder.setConnectionFactory(helper);
        builder.addRowSetConsumer(int[].class, ArrayIntConsumer.class);
        builder.addMapper(new EnumToStringMapper());
        builder.addMapper(new UriTypeMap());
        storage = builder.build();

        final IDBInitAH initAH = storage.get(IDBInitAH.class);
        initAH.createDB();
    }

    @After
    public void dropDatabase() throws StorageException {
        storage.shutdown();
    }

    @Test
    public void readEnums() throws Exception {
        IEnumsAH eAH = storage.get(IEnumsAH.class);

        final List<FirstEnum> first = eAH.getFirst();
        Assert.assertEquals(1, first.size());
        Assert.assertEquals(FirstEnum.class, first.get(0).getClass());
        Assert.assertEquals(FirstEnum.One, first.get(0));

        final List<SecondEnum> second = eAH.getSecond();
        Assert.assertEquals(1, second.size());
        Assert.assertEquals(SecondEnum.class, second.get(0).getClass());
        Assert.assertEquals(SecondEnum.Unu, second.get(0));

        final List<ThirdEnum> third = eAH.getThird();
        Assert.assertEquals(1, third.size());
        Assert.assertEquals(ThirdEnum.class, third.get(0).getClass());
        Assert.assertEquals(ThirdEnum.Un, third.get(0));

    }
}
