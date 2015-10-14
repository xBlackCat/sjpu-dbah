package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.ExpandType;
import org.xblackcat.sjpu.storage.workflow.data.Numbers;

import java.time.LocalDateTime;

/**
 * 14.10.2015 14:13
 *
 * @author xBlackCat
 */
public interface IConverterToys {
    int simpleMethod(int a, int b, int c) throws StorageException;

    int withTypeMappers(int a, LocalDateTime date, Numbers num) throws StorageException;

    @ExpandType(type = NamedItem.class, fields = {"id", "name"})
    int withExpandingAnn(int a, NamedItem item, NamedItem item2) throws StorageException;

    int withAutoExpandingClass(int a, AutoNamedItem item, AutoNamedItem item2) throws StorageException;

    @ExpandType(type = NamedItem.class, fields = {"id", "name"})
    @ExpandType(type = AutoNamedItem.class, fields = {"name"})
    int overrideExpandingClass(int a, NamedItem item, AutoNamedItem item2) throws StorageException;
}
