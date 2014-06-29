package org.xblackcat.sjpu.storage.workflow;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.ann.SqlOptArg;
import org.xblackcat.sjpu.storage.ann.SqlPart;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;

import java.util.List;

/**
 * 22.04.2014 18:10
 *
 * @author xBlackCat
 */
public interface IDataAH extends IAH {
    @Sql("SELECT\n" +
                 "  name\n" +
                 "FROM list\n" +
                 "WHERE id = ?")
    String get(Integer id) throws StorageException;

    @Sql("INSERT INTO list (id, name) VALUES (?, ?)")
    void put(int id, String name) throws StorageException;

    @Sql("SELECT\n" +
                 "  id\n" +
                 "FROM list\n" +
                 "ORDER BY id")
    int[] getIds() throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n" +
                 "WHERE id = ?")
    Element getElement(Integer id) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM {0}\n" +
                 "WHERE id = ?")
    Element getElement(@SqlPart String tableName, Integer id) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM {0}\n" +
                 "WHERE id = ?")
    Element getElement(Integer id, @SqlPart String tableName) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n" +
                 "WHERE id = ?")
    @MapRowTo(Element.class)
    IElement<String> getIElement(Integer id) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list")
    @MapRowTo(Element.class)
    List<Element> getListElement() throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list {0}")
    @MapRowTo(Element.class)
    List<Element> getListElement(@SqlPart @SqlOptArg("WHERE id = ?") Integer id) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list")
    @MapRowTo(Element.class)
    List<IElement<String>> getListIElement() throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list \n" +
                 "WHERE\n" +
                 "  TRUE {0} {0}")
    @MapRowTo(Element.class)
    List<IElement<String>> getListIElement(@SqlPart @SqlOptArg("AND id = ?") Integer id) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list")
    @MapRowTo(Element.class)
    void getListElement(IRowConsumer<Element> consumer) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list")
    @MapRowTo(Element.class)
    void getListIElement(IRowConsumer<IElement<String>> consumer) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n" +
                 "WHERE\n" +
                 "  id >= ?")
    @MapRowTo(Element.class)
    void getListElement(IRowConsumer<Element> consumer, int ind) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n" +
                 "WHERE\n" +
                 "  id >= ?")
    @MapRowTo(Element.class)
    void getListIElement(IRowConsumer<IElement<String>> consumer, int idx) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n" +
                 "WHERE\n" +
                 "  id >= ?")
    @MapRowTo(Element.class)
    void getListElement(int ind, IRowConsumer<Element> consumer) throws StorageException;

    @Sql("SELECT\n" +
                 "  id, name\n" +
                 "FROM list\n" +
                 "WHERE\n" +
                 "  id >= ?")
    @MapRowTo(Element.class)
    void getListIElement(int ind, IRowConsumer<IElement<String>> consumer) throws StorageException;

    @Sql("DELETE FROM list")
    void dropElements() throws StorageException;
}
