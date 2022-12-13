package org.xblackcat.sjpu.storage.workflow.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;
import org.xblackcat.sjpu.storage.workflow.base.IDBInitAH;
import org.xblackcat.sjpu.storage.workflow.base.IDataAH;
import org.xblackcat.sjpu.storage.workflow.data.ArrayIntConsumer;
import org.xblackcat.sjpu.storage.workflow.data.Element;
import org.xblackcat.sjpu.storage.workflow.data.ElementNumber;
import org.xblackcat.sjpu.storage.workflow.data.Numbers;

import java.util.List;

/**
 * 19.12.13 12:33
 *
 * @author xBlackCat
 */
public class WorkflowFuncTest {
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
    public void processPlainData() throws StorageException {
        final IDataAH dataAH = storage.get(IDataAH.class);

        dataAH.dropElements();

        for (Numbers n : Numbers.values()) {
            dataAH.put(n.ordinal(), n.name());
        }

        {
            final IListFuncAH funcAH = storage.get(IListFuncAH.class, "SELECT id, name FROM list");
            final List<Element> list = funcAH.getList();
            Assertions.assertNotNull(list);
            Assertions.assertEquals(Numbers.values().length, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }

        }

        {
            final IListFuncAH funcAH = storage.get(IListFuncAH.class, "SELECT id, name FROM list WHERE id < 5");
            final List<Element> list = funcAH.getList();
            Assertions.assertNotNull(list);
            Assertions.assertEquals(5, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final IGetFuncAH funcAH = storage.get(IGetFuncAH.class,  "SELECT id, name FROM list WHERE id = ?");
            final ElementNumber element = funcAH.getElement(5);
            Assertions.assertNotNull(element);
            Assertions.assertEquals(Numbers.Five, element.getName());
        }
    }

}
