package org.xblackcat.sjpu.storage.bugfix.fewenums;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
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

    @AfterEach
    public void dropDatabase() throws StorageException {
        storage.shutdown();
    }

    @Test
    public void readEnums() throws Exception {
        IEnumsAH eAH = storage.get(IEnumsAH.class);

        final List<FirstEnum> first = eAH.getFirst();
        Assertions.assertEquals(1, first.size());
        Assertions.assertEquals(FirstEnum.class, first.get(0).getClass());
        Assertions.assertEquals(FirstEnum.One, first.get(0));

        final List<SecondEnum> second = eAH.getSecond();
        Assertions.assertEquals(1, second.size());
        Assertions.assertEquals(SecondEnum.class, second.get(0).getClass());
        Assertions.assertEquals(SecondEnum.Unu, second.get(0));

        final List<ThirdEnum> third = eAH.getThird();
        Assertions.assertEquals(1, third.size());
        Assertions.assertEquals(ThirdEnum.class, third.get(0).getClass());
        Assertions.assertEquals(ThirdEnum.Un, third.get(0));

    }
}
