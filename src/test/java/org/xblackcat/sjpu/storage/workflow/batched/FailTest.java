package org.xblackcat.sjpu.storage.workflow.batched;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.workflow.data.IElement;

/**
 * 27.08.2014 12:24
 *
 * @author xBlackCat
 */
public class FailTest {
    @Test
    public void testInvalidConsumer() throws StorageException {
        IStorage s = StorageBuilder.defaultStorage(Config.TEST_DB_CONFIG);
        try {
            final ITestAH testAH = s.get(ITestAH.class);

            Assertions.fail("GeneratorException expected");
        } catch (GeneratorException e) {
            Assertions.assertTrue(true); // Exception occur
        }
    }

    public interface ITestAH extends IAH {
        @Sql("SELECT 1")
        /*@MapRowTo(String.class) - check missing annotation*/
        void getList(IRowConsumer rowConsumer) throws StorageException;
    }
    @Test
    public void testInvalidArg() throws StorageException {
        IStorage s = StorageBuilder.defaultStorage(Config.TEST_DB_CONFIG);
        try {
            final ITestArgAH testAH = s.get(ITestArgAH.class);

            Assertions.fail("GeneratorException expected");
        } catch (GeneratorException e) {
            Assertions.assertTrue(true); // Exception occur
        }
    }

    public interface ITestArgAH extends IAH {
        @Sql("SELECT 1")
        /*@MapRowTo(String.class) - check missing annotation*/
        void getList(IRowConsumer<IElement> rowConsumer) throws StorageException;
    }
}
