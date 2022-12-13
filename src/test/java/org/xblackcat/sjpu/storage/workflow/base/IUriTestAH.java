package org.xblackcat.sjpu.storage.workflow.base;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.workflow.data.IElement;
import org.xblackcat.sjpu.storage.workflow.data.Uri;

import java.net.URI;
import java.util.List;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public interface IUriTestAH extends IAH {
    @Sql("INSERT INTO uri (uri) VALUES (?)")
    Integer putUri(URI uri) throws StorageException;

    @Sql("INSERT INTO uri (id, uri) VALUES (?,?)")
    int putUri(int i, URI uri) throws StorageException;

    @Sql("INSERT INTO uri (uri) VALUES (?)")
    @MapRowTo(Integer.class)
    int putUri(IRowConsumer<Integer> i, URI uri) throws StorageException;

    @Sql("SELECT\n" +
                 "  uri\n" +
                 "FROM uri\n" +
                 "WHERE id = ?")
    URI get(int id) throws StorageException;

    @Sql("SELECT\n" +
                 "  id,\n" +
                 "  uri\n" +
                 "FROM uri\n" +
                 "WHERE id = ?")
    @MapRowTo(Uri.class)
    IElement<URI> getElement(int id) throws StorageException;

    @Sql("SELECT\n" +
                 "  id,\n" +
                 "  uri\n" +
                 "FROM uri\n")

    @MapRowTo(Uri.class)
    List<IElement<URI>> getList() throws StorageException;
}
