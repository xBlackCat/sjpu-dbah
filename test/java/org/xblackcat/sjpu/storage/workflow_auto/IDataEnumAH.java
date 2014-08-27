package org.xblackcat.sjpu.storage.workflow_auto;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.*;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public interface IDataEnumAH extends IAH {
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
                 "FROM list {0}")
    List<ElementNumber> getListElement(@SqlPart @SqlOptArg("WHERE name = ?") Numbers name) throws StorageException;

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
    @MapRowTo(ElementNumber.class)
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
    @MapRowTo(ElementNumber.class)
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
