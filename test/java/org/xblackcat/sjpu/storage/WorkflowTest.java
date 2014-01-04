package org.xblackcat.sjpu.storage;

import org.h2.Driver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.connection.IDatabaseSettings;
import org.xblackcat.sjpu.storage.impl.Storage;
import org.xblackcat.sjpu.storage.typemap.EnumToStringMapper;

import java.net.URI;
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
        storage = new Storage(helper, new EnumToStringMapper(), new UriTypeMap());

        final IDBInitAH initAH = storage.get(IDBInitAH.class);
        initAH.init1();
        initAH.init2();
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

            final IElement<String> iElement = dataAH.getIElement(n.ordinal());
            Assert.assertNotNull(iElement);
            Assert.assertEquals(n.name(), iElement.getName());
            Assert.assertEquals(n.ordinal(), iElement.getId());
        }

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

        {
            final List<ElementNumber> list = dataAH.getListElement();
            for (ElementNumber el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.id], el.name);
            }
        }

        {
            final List<IElement<Numbers>> list = dataAH.getListIElement();
            for (IElement<Numbers> el : list) {
                Assert.assertNotNull(list);
                Assert.assertEquals(Numbers.values()[el.getId()], el.getName());
            }
        }
    }

    @Test
    public void processDefinedClasses() throws StorageException {
        final IUriTest uriTest = storage.get(IUriTest.class);

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

    public static interface IDBInitAH extends IAH {
        @Sql("CREATE TABLE \"list\" (\"id\" INT, \"name\" TEXT, PRIMARY KEY (\"id\"))")
        void init1() throws StorageException;

        @Sql("CREATE TABLE \"uri\" (\"id\" INT AUTO_INCREMENT, \"uri\" TEXT, PRIMARY KEY (\"id\"))")
        void init2() throws StorageException;
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
                     "FROM \"list\"\n" +
                     "WHERE \"id\" = ?")
        @MapRowTo(Element.class)
        IElement<String> getIElement(int id) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"")
        @MapRowTo(Element.class)
        List<Element> getListElement() throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"")
        @MapRowTo(Element.class)
        List<IElement<String>> getListIElement() throws StorageException;

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
                     "FROM \"list\"\n" +
                     "WHERE \"id\" = ?")
        @MapRowTo(ElementNumber.class)
        IElement<Numbers> getIElement(int id) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"")
        @MapRowTo(ElementNumber.class)
        List<ElementNumber> getListElement() throws StorageException;

        @Sql("SELECT\n" +
                     "  \"name\"\n" +
                     "FROM \"list\"")
        @MapRowTo(Numbers.class)
        List<Numbers> getList() throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\", \"name\"\n" +
                     "FROM \"list\"")
        @MapRowTo(ElementNumber.class)
        List<IElement<Numbers>> getListIElement() throws StorageException;

        @Sql("DELETE FROM \"list\"")
        void dropElements() throws StorageException;
    }

    public static interface IUriTest extends IAH {
        @Sql("INSERT INTO \"uri\" (\"uri\") VALUES (?)")
        int putUri(URI uri) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"uri\"\n" +
                     "FROM \"uri\"\n" +
                     "WHERE \"id\" = ?")
        URI get(int id) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\",\n" +
                     "  \"uri\"\n" +
                     "FROM \"uri\"\n" +
                     "WHERE \"id\" = ?")
        @MapRowTo(Uri.class)
        IElement<URI> getElement(int id) throws StorageException;

        @Sql("SELECT\n" +
                     "  \"id\",\n" +
                     "  \"uri\"\n" +
                     "FROM \"uri\"\n")

        @MapRowTo(Uri.class)
        List<IElement<URI>> getList() throws StorageException;
    }

    public static class Element implements IElement<String> {
        public final int id;
        public final String name;

        public Element(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
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

    public static class ElementNumber implements IElement<Numbers> {
        public final int id;
        public final Numbers name;

        public ElementNumber(int id, Numbers name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public Numbers getName() {
            return name;
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

    public static interface IElement<T> {
        int getId();

        T getName();
    }

    public static class Uri implements IElement<URI> {
        public final int id;
        public final URI name;

        public Uri(int id, URI name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public URI getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Uri)) {
                return false;
            }

            Uri uri = (Uri) o;

            if (id != uri.id) {
                return false;
            }
            if (name != null ? !name.equals(uri.name) : uri.name != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
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
