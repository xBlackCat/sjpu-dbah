package org.xblackcat.sjpu.storage.workflow.auto;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.*;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.ann.SqlArg;
import org.xblackcat.sjpu.storage.consumer.IRowConsumer;
import org.xblackcat.sjpu.storage.workflow.data.IElement;

import java.net.URL;

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

            Assert.fail("GeneratorException expected");
        } catch (GeneratorException e) {
            Assert.assertEquals(
                    "Exception occurs while building method public abstract void org.xblackcat.sjpu.storage.workflow.auto.FailTest$ITestAH.getList(org.xblackcat.sjpu.storage.consumer.IRowConsumer) throws org.xblackcat.sjpu.storage.StorageException: Set target class with annotation interface org.xblackcat.sjpu.storage.ann.MapRowTo for method public abstract void org.xblackcat.sjpu.storage.workflow.auto.FailTest$ITestAH.getList(org.xblackcat.sjpu.storage.consumer.IRowConsumer) throws org.xblackcat.sjpu.storage.StorageException",
                    e.getMessage()
            ); // Exception occur
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

            Assert.fail("GeneratorException expected");
        } catch (GeneratorException e) {
            Assert.assertEquals(
                    "Exception occurs while building method public abstract void org.xblackcat.sjpu.storage.workflow.auto.FailTest$ITestArgAH.getList(org.xblackcat.sjpu.storage.consumer.IRowConsumer) throws org.xblackcat.sjpu.storage.StorageException: Row could be mapped only to non-abstract class",
                    e.getMessage()
            ); // Exception occur
        }
    }

    public interface ITestArgAH extends IAH {
        @Sql("SELECT 1")
        /*@MapRowTo(String.class) - check missing annotation*/
        void getList(IRowConsumer<IElement> rowConsumer) throws StorageException;
    }

    @Test
    public void testMissedExpanding() throws StorageException {
        IStorage s = StorageBuilder.defaultStorage(Config.TEST_DB_CONFIG);
        try {
            final ITestExpAH testAH = s.get(ITestExpAH.class);

            Assert.fail("GeneratorException expected");
        } catch (GeneratorException e) {
            Assert.assertEquals(
                    "Exception occurs while building method public abstract int org.xblackcat.sjpu.storage.workflow.auto.FailTest$ITestExpAH.getList(java.net.URL) throws org.xblackcat.sjpu.storage.StorageException: Can't process type java.net.URL",
                    e.getMessage()
            ); // Exception occur
        }
    }

    public interface ITestExpAH extends IAH {
        @Sql("SELECT ?")
//        @ExpandType(type = URL.class, fields = "toExternalForm")
        int getList(URL url) throws StorageException;
    }

    @Test
    public void testMissedExpanding2() throws StorageException {
        IStorage s = StorageBuilder.defaultStorage(Config.TEST_DB_CONFIG);
        try {
            final ITestExp2AH testAH = s.get(ITestExp2AH.class);

            Assert.fail("GeneratorException expected");
        } catch (GeneratorException e) {
            Assert.assertEquals(
                    "Exception occurs while building method public abstract int org.xblackcat.sjpu.storage.workflow.auto.FailTest$ITestExp2AH.getList(java.net.URL) throws org.xblackcat.sjpu.storage.StorageException: Can't process type java.net.URL",
                    e.getMessage()
            ); // Exception occur
        }
    }

    public interface ITestExp2AH extends IAH {
        @Sql("SELECT {0}")
//        @ExpandType(type = URL.class, fields = "toExternalForm")
        int getList(@SqlArg(0) URL url) throws StorageException;
    }
}
