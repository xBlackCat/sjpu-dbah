package org.xblackcat.sjpu.storage.workflow;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRawProcessor;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 19.12.13 12:33
 *
 * @author xBlackCat
 */
public class WorkflowTest {
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
        Assert.assertNull(dataAH.getElement(null));
        Assert.assertNull(dataAH.getIElement(null));


        {
            final List<Element> list = dataAH.getListElement();
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
            IRowConsumer<Element> consumer = new IRowConsumer<Element>() {
                @Override
                public boolean consume(Element o) throws ConsumeException {
                    Assert.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                    return false;
                }
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        {
            IRowConsumer<IElement<String>> consumer = new IRowConsumer<IElement<String>>() {
                @Override
                public boolean consume(IElement<String> o) throws ConsumeException {
                    Assert.assertEquals(Numbers.values()[o.getId()].name(), o.getName());
                    return false;
                }
            };
            dataAH.getListIElement(consumer);
            dataAH.getListIElement(consumer, 0);
            dataAH.getListIElement(0, consumer);
        }

        {
            int[] expect = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
            int[] got = dataAH.getIds();
            Assert.assertArrayEquals(expect, got);
        }
    }

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

            final IElement<Numbers> iElement = dataAH.getIElement(n.ordinal());
            Assert.assertNotNull(iElement);
            Assert.assertEquals(n, iElement.getName());
            Assert.assertEquals(n.ordinal(), iElement.getId());
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
            IRowConsumer<ElementNumber> consumer = new IRowConsumer<ElementNumber>() {
                @Override
                public boolean consume(ElementNumber o) throws ConsumeException {
                    Assert.assertEquals(Numbers.values()[o.getId()], o.getName());
                    return false;
                }
            };
            dataAH.getListElement(consumer);
            dataAH.getListElement(consumer, 0);
            dataAH.getListElement(0, consumer);
        }

        {
            IRowConsumer<IElement<Numbers>> consumer = new IRowConsumer<IElement<Numbers>>() {
                @Override
                public boolean consume(IElement<Numbers> o) throws ConsumeException {
                    Assert.assertEquals(Numbers.values()[o.getId()], o.getName());
                    return false;
                }
            };
            dataAH.getListIElement(consumer);
            dataAH.getListIElement(consumer, 0);
            dataAH.getListIElement(0, consumer);
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
    public void processRaw() throws StorageException {
        final IDataRawAH dataAH = storage.get(IDataRawAH.class);

        dataAH.dropElements();

        for (Numbers n : Numbers.values()) {
            dataAH.put(n.ordinal(), n);
        }

        IRawProcessor rawProcessor = new TestRawProcessor();
        IRawProcessor rawProcessor5 = new TestRawProcessor(5);

        dataAH.getListElement(rawProcessor);
        dataAH.getListElement(rawProcessor, "list");
        dataAH.getListElement("list", rawProcessor);

        dataAH.getListElement(rawProcessor5, 5);
        dataAH.getListElement(rawProcessor5, "list", 5);
        dataAH.getListElement(rawProcessor5, 5, "list");
        dataAH.getListElement("list", rawProcessor5, 5);

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

        for (IElement<URI> uriElement : uriTest.getList()) {
            IElement<URI> uri = uriTest.getElement(uriElement.getId());

            Assert.assertEquals(uriElement, uri);
        }
    }

    private static class TestRawProcessor implements IRawProcessor {
        private final int count;

        private TestRawProcessor() {
            this(-1);
        }

        public TestRawProcessor(int i) {
            count = i;
        }

        @Override
        public void process(ResultSet rs) throws SQLException {
            int i = 0;
            while (rs.next()) {
                Assert.assertEquals(rs.getInt(1), Numbers.valueOf(rs.getString(2)).ordinal());
                Assert.assertEquals(rs.getString(2), Numbers.values()[rs.getInt(1)].name());
                i++;
            }

            if (count >= 0) {
                Assert.assertEquals(count, i);
            }
        }
    }
}
