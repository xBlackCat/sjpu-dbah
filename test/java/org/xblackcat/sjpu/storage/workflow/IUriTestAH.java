package org.xblackcat.sjpu.storage.workflow;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.Sql;

import java.net.URI;
import java.util.List;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public interface IUriTestAH extends IAH {
    @Sql("INSERT INTO uri (uri) VALUES (?)")
    int putUri(URI uri) throws StorageException;

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
