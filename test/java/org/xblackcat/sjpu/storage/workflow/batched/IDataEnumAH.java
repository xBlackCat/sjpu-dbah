package org.xblackcat.sjpu.storage.workflow.batched;

import org.xblackcat.sjpu.storage.IBatchedAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.RowSetConsumer;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.workflow.data.ElementNumber;
import org.xblackcat.sjpu.storage.workflow.data.EnumMapConsumer;
import org.xblackcat.sjpu.storage.workflow.data.IElement;
import org.xblackcat.sjpu.storage.workflow.data.Numbers;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 22.04.2014 18:10
 *
 * @author xBlackCat
 */
public interface IDataEnumAH extends IBatchedAH {
    @Sql("SELECT\n" +
            "  name\n" +
            "FROM list\n" +
            "WHERE id = ?")
    Numbers get(Integer id) throws StorageException;

    @Sql("INSERT INTO list (id, name) VALUES (?, ?)")
    void put(int id, Numbers element) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE id = ?")
    ElementNumber getElement(Integer id) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE id = ?")
    @MapRowTo(ElementNumber.class)
    IElement<Numbers> getIElement(Integer id) throws StorageException;

    @Sql("SELECT\n" +
            "  name\n" +
            "FROM list")
    List<Numbers> getList() throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list")
    List<ElementNumber> getListElement() throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list")
    Set<ElementNumber> getSetElement() throws StorageException;

    @Sql("SELECT\n" +
            "  name\n" +
            "FROM list")
    EnumSet<Numbers> getEnumSetElement() throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list")
    @MapRowTo(ElementNumber.class)
    List<IElement<Numbers>> getListIElement() throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n")
    void getListElement(IRowConsumer<ElementNumber> consumer) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n")
    @MapRowTo(ElementNumber.class)
    void getListIElement(IRowConsumer<IElement<Numbers>> consumer) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n")
    @MapRowTo(ElementNumber.class)
    @RowSetConsumer(EnumMapConsumer.class)
    Map<Numbers, Integer> getMapElement() throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE\n" +
            "  id >= ?")
    void getListElement(IRowConsumer<ElementNumber> consumer, int idx) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE\n" +
            "  id >= ?")
    @MapRowTo(ElementNumber.class)
    void getListIElement(IRowConsumer<IElement<Numbers>> consumer, int idx) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE\n" +
            "  id >= ?")
    @MapRowTo(ElementNumber.class)
    void getListIElementRaw(IRowConsumer<IElement> consumer, int idx) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE\n" +
            "  id >= ?")
    @MapRowTo(ElementNumber.class)
    void getListIElementRaw2(IRowConsumer<IElement<?>> consumer, int idx) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE\n" +
            "  id >= ?")
    void getListElement(int idx, IRowConsumer<ElementNumber> consumer) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list\n" +
            "WHERE\n" +
            "  id >= ?")
    @MapRowTo(ElementNumber.class)
    void getListIElement(int idx, IRowConsumer<IElement<Numbers>> consumer) throws StorageException;

    @Sql("DELETE FROM list")
    void dropElements() throws StorageException;
}
