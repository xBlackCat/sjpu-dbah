package org.xblackcat.sjpu.storage.workflow.base;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.*;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.workflow.data.Element;
import org.xblackcat.sjpu.storage.workflow.data.IElement;

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

    @Sql("INSERT INTO list (id, name) VALUES (?, ?)")
    @ExpandType(type = Element.class, fields = {"id", "name"})
    void put(Element element) throws StorageException;

    @Sql("INSERT INTO list (id, name) VALUES {0}")
    @ExpandType(type = Element.class, fields = {"id", "name"})
    void putAll(@SqlPart @SqlVarArg("(?, ?)") List<Element> elementList) throws StorageException;

    @Sql("INSERT INTO list (id, name) VALUES {0}")
    @ExpandType(type = Element.class, fields = {"id", "name"})
    void putAll(@SqlPart @SqlVarArg("(?, ?)") Element... elementList) throws StorageException;

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
            "FROM list WHERE id = {0} AND id = {0}")
    @MapRowTo(Element.class)
    List<Element> getListElement2(@SqlArg(0) Integer id) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list WHERE id = {0} OR id = ? OR id = {0} ORDER BY id")
    @MapRowTo(Element.class)
    List<Element> getListElement2(int id1, @SqlArg(0) Integer id) throws StorageException;

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
            "FROM list \n" +
            "WHERE\n" +
            "  id IN ({0})")
    @MapRowTo(Element.class)
    List<IElement<String>> getListIElementArray(@SqlPart @SqlVarArg int[] ids) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list \n" +
            "WHERE\n" +
            "  id IN ({0})")
    @MapRowTo(Element.class)
    List<IElement<String>> getListIElementVarArg(@SqlPart @SqlVarArg int... ids) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list \n" +
            "WHERE\n" +
            "  name IN ({0})")
    @MapRowTo(Element.class)
    List<IElement<String>> getListIElementVarArg(@SqlPart @SqlVarArg String... names) throws StorageException;

    @Sql("SELECT\n" +
            "  id, name\n" +
            "FROM list \n" +
            "WHERE\n" +
            "  id IN ({0})")
    @MapRowTo(Element.class)
    List<IElement<String>> getListIElementList(@SqlPart @SqlVarArg List<Integer> ids) throws StorageException;

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
