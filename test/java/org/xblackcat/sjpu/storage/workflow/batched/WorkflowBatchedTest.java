package org.xblackcat.sjpu.storage.workflow.batched;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.consumer.SingletonConsumer;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;
import org.xblackcat.sjpu.storage.workflow.base.IDBInitAH;
import org.xblackcat.sjpu.storage.workflow.data.*;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 19.12.13 12:33
 *
 * @author xBlackCat
 */
public class WorkflowBatchedTest {
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
        try (final IDataAH dataAH = storage.startBatch(IDataAH.class)) {

            dataAH.dropElements();

            for (Numbers n : Numbers.values()) {
                dataAH.put(n.ordinal(), n.name());
            }

            for (Numbers n : Numbers.values()) {
                Assertions.assertEquals(n.name(), dataAH.get(n.ordinal()));
            }
        }

        try (final IDataAH dataAH = storage.startBatch(IDataAH.class)) {
            for (Numbers n : Numbers.values()) {
                {
                    final Element element = dataAH.getElement(n.ordinal());
                    Assertions.assertNotNull(element);
                    Assertions.assertEquals(n.name(), element.name);
                    Assertions.assertEquals(n.ordinal(), element.id);
                }

                final IElement<String> iElement = dataAH.getIElement(n.ordinal());
                Assertions.assertNotNull(iElement);
                Assertions.assertEquals(n.name(), iElement.getName());
                Assertions.assertEquals(n.ordinal(), iElement.getId());
            }

            Assertions.assertNull(dataAH.get(null));
            Assertions.assertNull(dataAH.getElement(null));
            Assertions.assertNull(dataAH.getIElement(null));

        }
        try (final IDataAH dataAH = storage.startBatch(IDataAH.class)) {
            final List<Element> list = dataAH.getListElement();
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        try (final IDataAH dataAH = storage.startBatch(IDataAH.class)) {
            final List<IElement<String>> list = dataAH.getListIElement();
            for (IElement<String> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        try (final IDataAH dataAH = storage.startBatch(IDataAH.class)) {
            IRowConsumer<Element> consumer = o -> {
                Assertions.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                return false;
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        try (final IDataAH dataAH = storage.startBatch(IDataAH.class)) {
            IRowConsumer<IElement<String>> consumer = o -> {
                Assertions.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                return false;
            };
            dataAH.getListIElement(consumer);
            dataAH.getListIElement(consumer, 0);
            dataAH.getListIElement(0, consumer);
        }

        try (final IDataAH dataAH = storage.startBatch(IDataAH.class)) {
            int[] expect = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            int[] got = dataAH.getIds();
            Assertions.assertArrayEquals(expect, got);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processEnumData() throws StorageException {
        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            dataAH.dropElements();

            for (Numbers n : Numbers.values()) {
                dataAH.put(n.ordinal(), n);
            }
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            for (Numbers n : Numbers.values()) {
                Assertions.assertEquals(n, dataAH.get(n.ordinal()));
            }

            for (Numbers n : Numbers.values()) {
                final ElementNumber element = dataAH.getElement(n.ordinal());
                Assertions.assertNotNull(element);
                Assertions.assertEquals(n, element.name);
                Assertions.assertEquals(n.ordinal(), element.id);

                final IElement<Numbers> iElement = dataAH.getIElement(n.ordinal());
                Assertions.assertNotNull(iElement);
                Assertions.assertEquals(n, iElement.getName());
                Assertions.assertEquals(n.ordinal(), iElement.getId());
            }

            Assertions.assertNull(dataAH.get(null));
            Assertions.assertNull(dataAH.getElement(null));
            Assertions.assertNull(dataAH.getIElement(null));
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            final List<ElementNumber> list = dataAH.getListElement();
            Assertions.assertNotNull(list);
            for (ElementNumber el : list) {
                Assertions.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            final Set<ElementNumber> list = dataAH.getSetElement();
            Assertions.assertNotNull(list);
            for (ElementNumber el : list) {
                Assertions.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            final EnumSet<Numbers> list = dataAH.getEnumSetElement();
            Assertions.assertNotNull(list);
            for (Numbers el : Numbers.values()) {
                Assertions.assertTrue(list.contains(el));
            }
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            final List<IElement<Numbers>> list = dataAH.getListIElement();
            for (IElement<Numbers> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()], el.getName());
            }
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            IRowConsumer<ElementNumber> consumer = o -> {
                Assertions.assertEquals(Numbers.values()[o.getId()], o.getName());
                return false;
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            IRowConsumer<IElement<Numbers>> consumer = o -> {
                Assertions.assertEquals(Numbers.values()[o.getId()], o.getName());
                return false;
            };
            dataAH.getListIElement(consumer);
            dataAH.getListIElement(consumer, 0);
            dataAH.getListIElement(0, consumer);

            // Test overriding element types
            dataAH.getListIElementRaw(((IRowConsumer<IElement>) (IRowConsumer) consumer), 0);
            dataAH.getListIElementRaw2((IRowConsumer) consumer, 0);
        }

        try (final IDataEnumAH dataAH = storage.startBatch(IDataEnumAH.class)) {
            final Map<Numbers, Integer> ma = dataAH.getMapElement();
            Assertions.assertEquals(Numbers.values().length, ma.size());
            for (Map.Entry<Numbers, Integer> e : ma.entrySet()) {
                Assertions.assertEquals(e.getKey().ordinal(), e.getValue().intValue(), e.getKey() + " element");
            }
        }
    }

    @Test
    public void processDefinedClasses() throws StorageException {
        try (final IUriTestAH uriTest = storage.startBatch(IUriTestAH.class)) {

            for (int i = 0; i < 10; i++) {
                URI uri = URI.create("http://example.org/test/" + i);

                int id = uriTest.putUri(uri);

                URI loaded = uriTest.get(id);

                Assertions.assertEquals(uri, loaded);
            }
        }
        try (final IUriTestAH uriTest = storage.startBatch(IUriTestAH.class)) {

            for (int i = 20; i < 30; i++) {
                URI uri = URI.create("http://example.org/test/" + i);

                int rows = uriTest.putUri(i, uri);
                Assertions.assertEquals(1, rows);

                URI loaded = uriTest.get(i);

                Assertions.assertEquals(uri, loaded);
            }

        }
        try (final IUriTestAH uriTest = storage.startBatch(IUriTestAH.class)) {
            for (int i = 50; i < 55; i++) {
                URI uri = URI.create("http://example.org/test/" + i);

                final SingletonConsumer<Integer> idHolder = new SingletonConsumer<>();
                int rows = uriTest.putUri(idHolder, uri);
                Assertions.assertEquals(1, rows);

                URI loaded = uriTest.get(idHolder.getRowsHolder());

                Assertions.assertEquals(uri, loaded);
            }

        }
        try (final IUriTestAH uriTest = storage.startBatch(IUriTestAH.class)) {
            for (IElement<URI> uriElement : uriTest.getList()) {
                IElement<URI> uri = uriTest.getElement(uriElement.getId());

                Assertions.assertEquals(uriElement, uri);
            }
        }
    }
}
