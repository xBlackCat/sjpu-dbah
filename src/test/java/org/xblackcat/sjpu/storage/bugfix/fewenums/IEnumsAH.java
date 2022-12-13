package org.xblackcat.sjpu.storage.bugfix.fewenums;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;

import java.util.List;

/**
 * 23.09.2014 11:22
 *
 * @author xBlackCat
 */
public interface IEnumsAH extends IAH {
    @Sql("SELECT 'One'")
    List<FirstEnum> getFirst() throws StorageException;

    @Sql("SELECT 'Unu'")
    List<SecondEnum> getSecond() throws StorageException;

    @Sql("SELECT 'Un'")
    List<ThirdEnum> getThird() throws StorageException;
}
