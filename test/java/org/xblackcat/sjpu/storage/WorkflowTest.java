package org.xblackcat.sjpu.storage;

import org.h2.Driver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xblackcat.sjpu.storage.connection.IDatabaseSettings;
import org.xblackcat.sjpu.storage.impl.IQueryHelper;
import org.xblackcat.sjpu.storage.impl.Storage;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;

import java.util.List;

/**
 * 19.12.13 12:33
 *
 * @author xBlackCat
 */
public class WorkflowTest {
    private static IDatabaseSettings settings = new IDatabaseSettings() {
        @Override
        public String getDbJdbcDriverClass() {
            return Driver.class.getName();
        }

        @Override
        public String getDbConnectionUrlPattern() {
            return "jdbc:h2:mem:db1";
        }

        @Override
        public String getDbAccessUser() {
            return null;
        }

        @Override
        public String getDbAccessPassword() {
            return null;
        }

        @Override
        public int getDbPoolSize() {
            return 10;
        }
    };
    private IStorage storage;

    @Before
    public void setupDatabase() throws StorageException {
        IQueryHelper helper = StorageUtils.buildQueryHelper(settings);
        storage = new Storage(helper, new EnumToStringMapper());

        final IDBInitAH initAH = storage.get(IDBInitAH.class);
        initAH.init1();
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
            final Element element = dataAH.getElement(n.ordinal());
            Assert.assertNotNull(element);
            Assert.assertEquals(n.name(), element.name);
            Assert.assertEquals(n.ordinal(), element.id);
        }

        final List<Element> list = dataAH.getListElement();
        for (Element el : list) {
            Assert.assertNotNull(list);
            Assert.assertEquals(Numbers.values()[el.id].name(), el.name);
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
        }

        final List<ElementNumber> list = dataAH.getListElement();
        for (ElementNumber el : list) {
            Assert.assertNotNull(list);
            Assert.assertEquals(Numbers.values()[el.id], el.name);
        }

    }

    public static interface IDBInitAH extends IAH {
        @Sql("CREATE TABLE \"list\" (\"id\" INT, \"name\" TEXT, PRIMARY KEY (\"id\"))")
        void init1() throws StorageException;
    }

    public static interface IDataAH extends IAH {
        @Sql("SELECT\n" +
                     "  \"name\"\n" +
                     "FROM \"list\"\n" +
                     "WHERE \"id\" = ?")
        String get(int id) throws StorageException;

        @Sql("INSERT INTO \"list\" (\"id\", \"name\") VALUES (?, ?)")
        void put(int id, String name) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"\n" +
                     "WHERE \"id\" = ?")
        Element getElement(int id) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"")
        @MapRowTo(Element.class)
        List<Element> getListElement() throws StorageException;

        @Sql("DELETE FROM \"list\"")
        void dropElements() throws StorageException;
    }

    public static interface IDataEnumAH extends IAH {
        @Sql("SELECT\n" +
                     "  \"name\"\n" +
                     "FROM \"list\"\n" +
                     "WHERE \"id\" = ?")
        Numbers get(int id) throws StorageException;

        @Sql("INSERT INTO \"list\" (\"id\", \"name\") VALUES (?, ?)")
        void put(int id, Numbers element) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"\n" +
                     "WHERE \"id\" = ?")
        ElementNumber getElement(int id) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"")
        @MapRowTo(ElementNumber.class)
        List<ElementNumber> getListElement() throws StorageException;

        @Sql("DELETE FROM \"list\"")
        void dropElements() throws StorageException;
    }

    public static class Element {
        public final int id;
        public final String name;

        public Element(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Element)) {
                return false;
            }

            Element element = (Element) o;

            return !(name != null ? !name.equals(element.name) : element.name != null);

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    public static class ElementNumber {
        public final int id;
        public final Numbers name;

        public ElementNumber(int id, Numbers name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ElementNumber)) {
                return false;
            }

            ElementNumber that = (ElementNumber) o;

            return name == that.name;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    public static enum Numbers {
        One,
        Two,
        Three,
        Four,
        Five,
        Six,
        Seven,
        Eight,
        Nine,
        Ten
    }
}
