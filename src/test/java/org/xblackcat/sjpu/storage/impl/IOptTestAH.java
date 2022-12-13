package org.xblackcat.sjpu.storage.impl;

import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.ann.SqlOptArg;
import org.xblackcat.sjpu.storage.ann.SqlPart;

/**
 * 30.06.2014 0:34
 *
 * @author xBlackCat
 */
public interface IOptTestAH extends IAH {
    @Sql("SELECT * FROM {0}")
    int plainSubstitution(@SqlPart String tableName) throws StorageException;

    @Sql("SELECT * FROM {0} WHERE id = ?")
    int plainSubstitution(@SqlPart String tableName, int id) throws StorageException;

    @Sql("SELECT * FROM {0} WHERE id = ?")
    int plainSubstitution(int id, @SqlPart String tableName) throws StorageException;

    @Sql("SELECT * FROM \"table\" WHERE \"id\" IS NOT NULL {0}")
    int optionalSubstitution(@SqlPart @SqlOptArg("AND amount = ?") Integer amount) throws StorageException;

    @Sql("SELECT * FROM \"table\" WHERE \"id\" = ? {0}")
    int optionalSubstitution(@SqlPart @SqlOptArg("AND amount = ?") Integer amount, int id) throws StorageException;

    @Sql("SELECT * FROM \"table\" WHERE \"id\" = ? {0}")
    int optionalSubstitution(int id, @SqlPart @SqlOptArg("AND amount = ?") Integer amount) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id IS NOT NULL {0}")
    int mixedSubstitution(@SqlPart @SqlOptArg("AND amount = ?") Integer amount, @SqlPart(1) String tableName) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id IS NOT NULL {0}")
    int mixedSubstitution(@SqlPart(1) String tableName, @SqlPart @SqlOptArg("AND amount = ?") Integer amount) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id ? {0}")
    int mixedSubstitution(
            @SqlPart(1) String tableName,
            @SqlPart @SqlOptArg("AND amount = ?") Integer amount,
            int id
    ) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id ? {0}")
    int mixedSubstitution(
            @SqlPart @SqlOptArg("AND amount = ?") Integer amount,
            @SqlPart(1) String tableName,
            int id
    ) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id ? {0}")
    int mixedSubstitution(
            @SqlPart(1) String tableName,
            int id,
            @SqlPart @SqlOptArg("AND amount = ?") Integer amount
    ) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id ? {0}")
    int mixedSubstitution(
            @SqlPart @SqlOptArg("AND amount = ?") Integer amount,
            int id,
            @SqlPart(1) String tableName
    ) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id ? {0}")
    int mixedSubstitution(
            int id,
            @SqlPart(1) String tableName,
            @SqlPart @SqlOptArg("AND amount = ?") Integer amount
    ) throws StorageException;

    @Sql("SELECT * FROM {1} WHERE id ? {0}")
    int mixedSubstitution(
            int id,
            @SqlPart @SqlOptArg("AND amount = ?") Integer amount,
            @SqlPart(1) String tableName
    ) throws StorageException;

}
