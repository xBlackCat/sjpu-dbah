package org.xblackcat.sjpu.storage.workflow.auto;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    public void processPlainData() throws StorageException {
        final IDataAH dataAH = storage.get(IDataAH.class);

        dataAH.dropElements();

        for (Numbers n : Numbers.values()) {
            dataAH.put(n.ordinal(), n.name());
        }

        for (Numbers n : Numbers.values()) {
            Assert.assertEquals(n.name(), dataAH.get(n.ordinal()));
        }

        for (Numbers n : Numbers.values()) {
            {
                final Element element = dataAH.getElement(n.ordinal());
                Assert.assertNotNull(element);
                Assert.assertEquals(n.name(), element.name);
                Assert.assertEquals(n.ordinal(), element.id);

                final Element checkElement = dataAH.getElement(element);
                Assert.assertNotNull(checkElement);
                Assert.assertEquals(n.name(), checkElement.name);
                Assert.assertEquals(n.ordinal(), checkElement.id);
            }
            {
                final Element element = dataAH.getElement(n.ordinal(), "list");
                Assert.assertNotNull(element);
                Assert.assertEquals(n.name(), element.name);
                Assert.assertEquals(n.ordinal(), element.id);
            }
            {
                final Element element = dataAH.getElement("list", n.ordinal());
                Assert.assertNotNull(element);
                Assert.assertEquals(n.name(), element.name);
                Assert.assertEquals(n.ordinal(), element.id);
            }

            final IElement<String> iElement = dataAH.getIElement(n.ordinal());
            Assert.assertNotNull(iElement);
            Assert.assertEquals(n.name(), iElement.getName());
            Assert.assertEquals(n.ordinal(), iElement.getId());
        }

        Assert.assertNull(dataAH.get(null));
        Assert.assertNull(dataAH.getElement((Integer) null));
        Assert.assertNull(dataAH.getElement((Element) null));
        Assert.assertNull(dataAH.getIElement(null));


        {
            final List<Element> list = dataAH.getListElement();
            for (Element el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement((Integer) null);
            for (Element el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement(1);
            Assert.assertEquals(1, list.size());
            for (Element el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElement();
            for (IElement<String> el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElement((Integer) null);
            for (IElement<String> el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElement(1);
            Assert.assertEquals(1, list.size());
            for (IElement<String> el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            IRowConsumer<Element> consumer = o -> {
                Assert.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                return false;
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        {
            IRowConsumer<IElement<String>> consumer = o -> {
                Assert.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                return false;
            };
            dataAH.getListIElement(consumer);
            dataAH.getListIElement(consumer, 0);
            dataAH.getListIElement(0, consumer);
        }

        {
            int[] expect = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            int[] got = dataAH.getIds();
            Assert.assertArrayEquals(expect, got);
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
            Assert.assertEquals(n, dataAH.get(n.ordinal()));
        }

        for (Numbers n : Numbers.values()) {
            final ElementNumber element = dataAH.getElement(n.ordinal());
            Assert.assertNotNull(element);
            Assert.assertEquals(n, element.name);
            Assert.assertEquals(n.ordinal(), element.id);

            final ElementNumber elementByObj = dataAH.getElementByObject(element);
            Assert.assertNotNull(elementByObj);
            Assert.assertEquals(n, elementByObj.name);
            Assert.assertEquals(n.ordinal(), elementByObj.id);

            final ElementNumber elementByInt = dataAH.getElementByInterface(element);
            Assert.assertNotNull(elementByInt);
            Assert.assertEquals(n, elementByInt.name);
            Assert.assertEquals(n.ordinal(), elementByInt.id);

            final IElement<Numbers> iElement = dataAH.getIElement(n.ordinal());
            Assert.assertNotNull(iElement);
            Assert.assertEquals(n, iElement.getName());
            Assert.assertEquals(n.ordinal(), iElement.getId());

            try {
                dataAH.getElementInvalid(n.ordinal());
                Assert.fail("Expecting a StorageException");
            } catch (StorageException e) {
                Assert.assertEquals("Can not execute query SELECT\n" +
                                            "  id, name\n" +
                                            "FROM list\n" +
                                            "WHERE id = /* $1 = (java.lang.Integer)*/ " + n.ordinal() + "  ORDER ", e.getMessage());
            }

            try {
                dataAH.getElementByObjectInvalid(element);
                Assert.fail("Expecting a StorageException");
            } catch (StorageException e) {
                Assert.assertEquals(
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
            Assert.fail("Expecting a StorageException");
        } catch (StorageException e) {
            Assert.assertEquals("Can not execute query SELECT\n" +
                                        "  id, name\n" +
                                        "FROM list\n" +
                                        "WHERE id = /* $1 = (java.lang.Integer)*/ NULL  ORDER ", e.getMessage());
        }

        try {
            dataAH.getElementByObjectInvalid(null);
            Assert.fail("Expecting a StorageException");
        } catch (StorageException e) {
            Assert.assertEquals("Can not execute query SELECT\n" +
                                        "  id, name\n" +
                                        "FROM list\n" +
                                        "WHERE id = /* $1#getId() = (int)*/ NULL and name = /* $1#getName() = (org.xblackcat.sjpu.storage.workflow.data.Numbers)*/ NULL ORDER ", e.getMessage());
        }

        Assert.assertNull(dataAH.get(null));
        Assert.assertNull(dataAH.getElement(null));
        Assert.assertNull(dataAH.getIElement(null));

        {
            final List<ElementNumber> list = dataAH.getListElement();
            Assert.assertNotNull(list);
            for (ElementNumber el : list) {
                Assert.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final List<ElementNumber> list = dataAH.getListElement((Numbers) null);
            Assert.assertNotNull(list);
            for (ElementNumber el : list) {
                Assert.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final List<ElementNumber> list = dataAH.getListElement(Numbers.One);
            Assert.assertNotNull(list);
            Assert.assertEquals(1, list.size());

            for (ElementNumber el : list) {
                Assert.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final Set<ElementNumber> list = dataAH.getSetElement();
            Assert.assertNotNull(list);
            for (ElementNumber el : list) {
                Assert.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final EnumSet<Numbers> list = dataAH.getEnumSetElement();
            Assert.assertNotNull(list);
            for (Numbers el : Numbers.values()) {
                Assert.assertTrue(list.contains(el));
            }
        }

        {
            final List<IElement<Numbers>> list = dataAH.getListIElement();
            for (IElement<Numbers> el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.getId()], el.getName());
            }
        }

        {
            IRowConsumer<ElementNumber> consumer = o -> {
                Assert.assertEquals(Numbers.values()[o.getId()], o.getName());
                return false;
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        {
            IRowConsumer<IElement<Numbers>> consumer = o -> {
                Assert.assertEquals(Numbers.values()[o.getId()], o.getName());
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
            Assert.assertEquals(Numbers.values().length, ma.size());
            for (Map.Entry<Numbers, Integer> e : ma.entrySet()) {
                Assert.assertEquals(e.getKey() + " element", e.getKey().ordinal(), e.getValue().intValue());
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

            Assert.assertEquals(uri, loaded);
        }

        for (int i = 20; i < 30; i++) {
            URI uri = URI.create("http://example.org/test/" + i);

            int rows = uriTest.putUri(i, uri);
            Assert.assertEquals(1, rows);

            URI loaded = uriTest.get(i);

            Assert.assertEquals(uri, loaded);
        }

        for (int i = 50; i < 60; i++) {
            URI uri = URI.create("http://example.org/test/" + i);

            final SingletonConsumer<Integer> idHolder = new SingletonConsumer<>();
            int rows = uriTest.putUri(idHolder, uri);
            Assert.assertEquals(1, rows);

            URI loaded = uriTest.get(idHolder.getRowsHolder());

            Assert.assertEquals(uri, loaded);
        }

        for (IElement<URI> uriElement : uriTest.getList()) {
            IElement<URI> uri = uriTest.getElement(uriElement.getId());

            Assert.assertEquals(uriElement, uri);
        }
    }
}
