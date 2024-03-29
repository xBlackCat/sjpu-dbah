package org.xblackcat.sjpu.storage.workflow.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;
import org.xblackcat.sjpu.storage.consumer.IRawProcessor;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.consumer.SingletonConsumer;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;
import org.xblackcat.sjpu.storage.workflow.data.*;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 19.12.13 12:33
 *
 * @author xBlackCat
 */
public class WorkflowBaseTest {
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

        dataAH.dropElements();

        dataAH.putAll(Stream.of(Numbers.values()).map(n -> new Element(n.ordinal(), n.name())).toArray(Element[]::new));

        for (Numbers n : Numbers.values()) {
            Assertions.assertEquals(n.name(), dataAH.get(n.ordinal()));
        }

        dataAH.dropElements();

        dataAH.putAll(Stream.of(Numbers.values()).map(n -> new Element(n.ordinal(), n.name())).collect(Collectors.toList()));

        for (Numbers n : Numbers.values()) {
            Assertions.assertEquals(n.name(), dataAH.get(n.ordinal()));
        }

        dataAH.dropElements();

        for (Numbers n : Numbers.values()) {
            dataAH.put(new Element(n.ordinal(), n.name()));
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
        Assertions.assertNull(dataAH.getElement(null));
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
            Assertions.assertEquals(11, list.size());
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
            final List<Element> list = dataAH.getListElement2(null);
            Assertions.assertEquals(0, list.size());
        }

        {
            final List<Element> list = dataAH.getListElement2(1);
            Assertions.assertEquals(1, list.size());
            for (Element el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.id].name(), el.name);
            }
        }

        {
            final List<Element> list = dataAH.getListElement2(2, null);
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(Numbers.values()[2].name(), list.get(0).name);
        }

        {
            final List<Element> list = dataAH.getListElement2(2, 1);
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(Numbers.values()[1].name(), list.get(0).name);
            Assertions.assertEquals(Numbers.values()[2].name(), list.get(1).name);
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
            final List<IElement<String>> list = dataAH.getListIElementVarArg(1, 3, 5);
            Assertions.assertEquals(3, list.size());
            for (IElement<String> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElementVarArg(Numbers.Eight.name(), Numbers.Zero.name());
            Assertions.assertEquals(2, list.size());
            for (IElement<String> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElementArray(new int[]{1, 3, 5});
            Assertions.assertEquals(3, list.size());
            for (IElement<String> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()].name(), el.getName());
            }
        }

        {
            final List<IElement<String>> list = dataAH.getListIElementList(Arrays.asList(1, 3, 5));
            Assertions.assertEquals(3, list.size());
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

        dataAH.dropElements();

        dataAH.putAll(Stream.of(Numbers.values()).map(n -> new ElementNumber(n.ordinal(), n)).toArray(ElementNumber[]::new));

        for (Numbers n : Numbers.values()) {
            Assertions.assertEquals(n, dataAH.get(n.ordinal()));
        }

        dataAH.dropElements();

        dataAH.putAll(Stream.of(Numbers.values()).map(n -> new ElementNumber(n.ordinal(), n)).collect(Collectors.toList()));

        for (Numbers n : Numbers.values()) {
            Assertions.assertEquals(n, dataAH.get(n.ordinal()));
        }

        dataAH.dropElements();

        for (Numbers n : Numbers.values()) {
            dataAH.put(new ElementNumber(n.ordinal(), n));
        }

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
            final ElementNumber element = dataAH.getDynamicElement("list", Numbers.One);
            Assertions.assertNotNull(element);
            Assertions.assertEquals(Numbers.One, element.getName());
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
        }

        {
            final Map<Numbers, Integer> ma = dataAH.getMapElement();
            Assertions.assertEquals(Numbers.values().length, ma.size());
            for (Map.Entry<Numbers, Integer> e : ma.entrySet()) {
                Assertions.assertEquals(e.getKey().ordinal(), e.getValue().intValue(), e.getKey() + " element");
            }
        }
        {
            final List<IElement<Numbers>> list = dataAH.getListIElementVarArg(Numbers.Eight.name(), Numbers.Zero.name());
            Assertions.assertEquals(2, list.size());
            for (IElement<Numbers> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()], el.getName());
            }
        }
        {
            final List<IElement<Numbers>> list = dataAH.getListIElementVarArg(Numbers.Eight, Numbers.Zero, Numbers.Nine);
            Assertions.assertEquals(3, list.size());
            for (IElement<Numbers> el : list) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(Numbers.values()[el.getId()], el.getName());
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

        dataAH.getListElement(rawProcessor5, 6);
        dataAH.getListElement(rawProcessor5, "list", 6);
        dataAH.getListElement(rawProcessor5, 6, "list");
        dataAH.getListElement("list", rawProcessor5, 6);

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
                Assertions.assertEquals(rs.getInt(1), Numbers.valueOf(rs.getString(2)).ordinal());
                Assertions.assertEquals(rs.getString(2), Numbers.values()[rs.getInt(1)].name());
                i++;
            }

            if (count >= 0) {
                Assertions.assertEquals(count, i);
            }
        }
    }
}
