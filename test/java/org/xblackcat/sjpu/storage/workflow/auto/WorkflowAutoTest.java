package org.xblackcat.sjpu.storage.workflow.auto;

import org.apache.commons.lang3.Range;
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
public class WorkflowAutoTest {
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

        for (Numbers n : Numbers.values()) {
            Assertions.assertEquals(n.name(), dataAH.get(n.ordinal()));
        }

        for (Numbers n : Numbers.values()) {
            {
                final Element element = dataAH.getElement(n.ordinal());
                Assertions.assertNotNull(element);
                Assertions.assertEquals(n.name(), element.name);
                Assertions.assertEquals(n.ordinal(), element.id);

                final Element checkElement = dataAH.getElement(element);
                Assertions.assertNotNull(checkElement);
                Assertions.assertEquals(n.name(), checkElement.name);
                Assertions.assertEquals(n.ordinal(), checkElement.id);
            }
            {
                final Element element = dataAH.getElement(n.ordinal(), "list");
                Assertions.assertNotNull(element);
                Assertions.assertEquals(n.name(), element.name);
                Assertions.assertEquals(n.ordinal(), element.id);
            }
            {
                final Element element = dataAH.getElement("list", n.ordinal());
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
        Assertions.assertNull(dataAH.getElement((Integer) null));
        Assertions.assertNull(dataAH.getElement((Element) null));
        Assertions.assertNull(dataAH.getIElement(null));


        {
            final List<Element> list = dataAH.getListElement();
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement((Integer) null);
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement(1);
            Assertions.assertEquals(1, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement((Range<Integer>) null);
            Assertions.assertEquals(11, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            Range<Integer> range = Range.between(4, 6);
            final List<Element> list = dataAH.getListElement(range);
            Assertions.assertEquals(3, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement((IntRange) null);
            Assertions.assertEquals(11, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement(new IntRange(4, 6));
            Assertions.assertEquals(3, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElement();
            for (IElement<String> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElement((Integer) null);
            for (IElement<String> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElement(1);
            Assertions.assertEquals(1, list.size());
            for (IElement<String> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            IRowConsumer<Element> consumer = o -> {
                Assertions.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                return false;
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        {
            IRowConsumer<IElement<String>> consumer = o -> {
                Assertions.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                return false;
            };
            dataAH.getListIElement(consumer);
            dataAH.getListIElement(consumer, 0);
            dataAH.getListIElement(0, consumer);
        }

        {
            int[] expect = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            int[] got = dataAH.getIds();
            Assertions.assertArrayEquals(expect, got);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processEnumData() throws StorageException {
        final IDataEnumAH dataAH = storage.get(IDataEnumAH.class);

        dataAH.dropElements();

        for (Numbers n : Numbers.values()) {
            dataAH.put(n.ordinal(), n);
        }

        for (Numbers n : Numbers.values()) {
            Assertions.assertEquals(n, dataAH.get(n.ordinal()));
        }

        for (Numbers n : Numbers.values()) {
            final ElementNumber element = dataAH.getElement(n.ordinal());
            Assertions.assertNotNull(element);
            Assertions.assertEquals(n, element.name);
            Assertions.assertEquals(n.ordinal(), element.id);

            final ElementNumber elementByObj = dataAH.getElementByObject(element);
            Assertions.assertNotNull(elementByObj);
            Assertions.assertEquals(n, elementByObj.name);
            Assertions.assertEquals(n.ordinal(), elementByObj.id);

            final ElementNumber elementByInt = dataAH.getElementByInterface(element);
            Assertions.assertNotNull(elementByInt);
            Assertions.assertEquals(n, elementByInt.name);
            Assertions.assertEquals(n.ordinal(), elementByInt.id);

            final IElement<Numbers> iElement = dataAH.getIElement(n.ordinal());
            Assertions.assertNotNull(iElement);
            Assertions.assertEquals(n, iElement.getName());
            Assertions.assertEquals(n.ordinal(), iElement.getId());

            try {
                dataAH.getElementInvalid(n.ordinal());
                Assertions.fail("Expecting a StorageException");
            } catch (StorageException e) {
                Assertions.assertEquals(
                        "Can not execute query SELECT\n" +
                        "  id, name\n" +
                        "FROM list\n" +
                        "WHERE id = /* $1 = (java.lang.Integer)*/ " + n.ordinal() + "  ORDER ",
                        e.getMessage()
                );
            }

            try {
                dataAH.getElementByObjectInvalid(element);
                Assertions.fail("Expecting a StorageException");
            } catch (StorageException e) {
                Assertions.assertEquals(
                        "Can not execute query SELECT\n" +
                        "  id, name\n" +
                        "FROM list\n" +
                        "WHERE id = /* $1#getId() = (int)*/ " +
                        element.getId() +
                        " and name = /* $1#getName() = (org.xblackcat.sjpu.storage.workflow.data.Numbers)*/ " +
                        element.getName() +
                        " ORDER ",
                        e.getMessage()
                );
            }
        }

        try {
            dataAH.getElementInvalid(null);
            Assertions.fail("Expecting a StorageException");
        } catch (StorageException e) {
            Assertions.assertEquals("Can not execute query SELECT\n" +
                                                          "  id, name\n" +
                                                          "FROM list\n" +
                                                          "WHERE id = /* $1 = (java.lang.Integer)*/ NULL  ORDER ", e.getMessage());
        }

        try {
            dataAH.getElementByObjectInvalid(null);
            Assertions.fail("Expecting a StorageException");
        } catch (StorageException e) {
            Assertions.assertEquals(
                    "Can not execute query SELECT\n" +
                    "  id, name\n" +
                    "FROM list\n" +
                    "WHERE id = /* $1#getId() = (int)*/ NULL and name = /* $1#getName() = (org.xblackcat.sjpu.storage.workflow.data.Numbers)*/ NULL ORDER ",
                    e.getMessage()
            );
        }

        Assertions.assertNull(dataAH.get(null));
        Assertions.assertNull(dataAH.getElement(null));
        Assertions.assertNull(dataAH.getIElement(null));

        {
            final List<ElementNumber> list = dataAH.getListElement();
            Assertions.assertNotNull(list);
            for (ElementNumber el : list) {
                Assertions.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final List<ElementNumber> list = dataAH.getListElement((Numbers) null);
            Assertions.assertNotNull(list);
            for (ElementNumber el : list) {
                Assertions.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final List<ElementNumber> list = dataAH.getListElement(Numbers.One);
            Assertions.assertNotNull(list);
            Assertions.assertEquals(1, list.size());

            for (ElementNumber el : list) {
                Assertions.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final Set<ElementNumber> list = dataAH.getSetElement();
            Assertions.assertNotNull(list);
            for (ElementNumber el : list) {
                Assertions.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final EnumSet<Numbers> list = dataAH.getEnumSetElement();
            Assertions.assertNotNull(list);
            for (Numbers el : Numbers.values()) {
                Assertions.assertTrue(list.contains(el));
            }
        }

        {
            final List<IElement<Numbers>> list = dataAH.getListIElement();
            for (IElement<Numbers> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()], el.getName());
            }
        }

        {
            IRowConsumer<ElementNumber> consumer = o -> {
                Assertions.assertEquals(Numbers.values()[o.getId()], o.getName());
                return false;
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        {
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

        {
            final Map<Numbers, Integer> ma = dataAH.getMapElement();
            Assertions.assertEquals(Numbers.values().length, ma.size());
            for (Map.Entry<Numbers, Integer> e : ma.entrySet()) {
                Assertions.assertEquals(e.getKey().ordinal(), e.getValue().intValue(), e.getKey() + " element");
            }
        }
    }

    @Test
    public void processDefinedClasses() throws StorageException {
        final IUriTestAH uriTest = storage.get(IUriTestAH.class);

        for (int i = 0; i < 10; i++) {
            URI uri = URI.create("http://example.org/test/" + i);

            int id = uriTest.putUri(uri);

            URI loaded = uriTest.get(id);

            Assertions.assertEquals(uri, loaded);
        }

        for (int i = 20; i < 30; i++) {
            URI uri = URI.create("http://example.org/test/" + i);

            int rows = uriTest.putUri(i, uri);
            Assertions.assertEquals(1, rows);

            URI loaded = uriTest.get(i);

            Assertions.assertEquals(uri, loaded);
        }

        for (int i = 50; i < 55; i++) {
            URI uri = URI.create("http://example.org/test/" + i);

            final SingletonConsumer<Integer> idHolder = new SingletonConsumer<>();
            int rows = uriTest.putUri(idHolder, uri);
            Assertions.assertEquals(1, rows);

            URI loaded = uriTest.get(idHolder.getRowsHolder());

            Assertions.assertEquals(uri, loaded);
        }

        for (IElement<URI> uriElement : uriTest.getList()) {
            IElement<URI> uri = uriTest.getElement(uriElement.getId());

            Assertions.assertEquals(uriElement, uri);
        }
    }
}
