package org.xblackcat.sjpu.storage.impl;


import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.IAHFactory;
import org.xblackcat.sjpu.storage.IStorage;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.connection.IConnectionFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 16.08.13 14:04
 *
 * @author xBlackCat
 */
public class AHGeneratorTest {
    @Test
    public void classLoadingIssues() {
        IStorage s1 = new Storage(new ConnectionFactoryStub());
        IStorage s2 = new Storage(new ConnectionFactoryStub());

        final ITIntAH intAH1 = s1.get(ITIntAH.class);
        final ITIntAH intAH2 = s2.get(ITIntAH.class);

        Assert.assertNotEquals(intAH1.getClass().getClassLoader(), intAH2.getClass().getClassLoader());
        Assert.assertNotEquals(intAH1.getClass(), intAH2.getClass());

        Assert.assertEquals(intAH1.getClass().getClassLoader(), s1.get(ITIntAH.class).getClass().getClassLoader());
        Assert.assertEquals(intAH1.getClass(), s1.get(ITIntAH.class).getClass());
        Assert.assertTrue(intAH1.getClass() == s1.get(ITIntAH.class).getClass());
        Assert.assertTrue(intAH1 == s1.get(ITIntAH.class));
    }

    @Test
    public void checkPrimitiveFunctionalAHStructure() throws StorageException {
        IAHFactory storage = new Storage(new ConnectionFactoryStub());

        final IPrimitiveFunctionalAH testAH = storage.get(IPrimitiveFunctionalAH.class, "");

        Set<Method> availableMethods = new HashSet<>(Arrays.asList(testAH.getClass().getMethods()));

        // Remove standard methods
        for (Method m : Object.class.getMethods()) {
            Assert.assertTrue("Standard method is not found: " + m.getName(), availableMethods.remove(m));
        }

        NextMethod:
        for (Method m : IPrimitiveFunctionalAH.class.getMethods()) {
            Iterator<Method> iterator = availableMethods.iterator();
            while (iterator.hasNext()) {
                Method mm = iterator.next();

                if (m.getName().equals(mm.getName()) && Arrays.equals(m.getParameterTypes(), mm.getParameterTypes())) {
                    iterator.remove();
                    continue NextMethod;
                }
            }

            Assert.fail("Method " + m + " is not implemented");
        }

        Assert.assertEquals("Found unexpected extra methods" + availableMethods, 0, availableMethods.size());
    }

    @Test
    public void checkPrimitiveAHStructure() throws StorageException {
        IAHFactory storage = new Storage(new ConnectionFactoryStub());

        final IPrimitiveAH testAH = storage.get(IPrimitiveAH.class);

        Set<Method> availableMethods = new HashSet<>(Arrays.asList(testAH.getClass().getMethods()));

        // Remove standard methods
        for (Method m : Object.class.getMethods()) {
            Assert.assertTrue("Standard method is not found: " + m.getName(), availableMethods.remove(m));
        }

        NextMethod:
        for (Method m : IPrimitiveAH.class.getMethods()) {
            Iterator<Method> iterator = availableMethods.iterator();
            while (iterator.hasNext()) {
                Method mm = iterator.next();

                if (m.getName().equals(mm.getName()) && Arrays.equals(m.getParameterTypes(), mm.getParameterTypes())) {
                    iterator.remove();
                    continue NextMethod;
                }
            }

            Assert.fail("Method " + m + " is not implemented");
        }

        Assert.assertEquals("Found unexpected extra methods" + availableMethods, 0, availableMethods.size());
    }

    @Test
    public void checkOptionalArgGeneration() {
        IAHFactory storage = new Storage(new ConnectionFactoryStub());

        final IOptTestAH testAH = storage.get(IOptTestAH.class);
        Assert.assertNotNull(testAH);
    }

    /*    @Test
       public void generatePrimitiveAH() throws StorageException {
           {
               final byte one = (byte) 1;

               {
                   IAHFactory storage = new Storage(new QueryHelperStub(1l));
                   final ITLongAH testAH = storage.get(ITLongAH.class);
                   Assert.assertEquals(one, testAH.getLong());
                   Assert.assertEquals(Long.valueOf(one), testAH.getLongObject());
                   testAH.getLongObject(new AssertConsumer<>(Long.valueOf(one)));
   */
/*
                Assert.assertEquals(one, testAH.getLong2());
                Assert.assertEquals(Long.valueOf(one), testAH.getLongObject2());
*//*

            }

            {
                IAHFactory storage = new Storage(new QueryHelperStub(1));
                final ITIntAH testAH = storage.get(ITIntAH.class);
                Assert.assertEquals(one, testAH.getInt());
                Assert.assertEquals(Integer.valueOf(one), testAH.getInteger());
*/
/*
                Assert.assertEquals(one, testAH.getInt2());
                Assert.assertEquals(Integer.valueOf(one), testAH.getInteger2());
*//*

            }

            {
                IAHFactory storage = new Storage(new QueryHelperStub((short) 1));
                final ITShortAH testAH = storage.get(ITShortAH.class);
                Assert.assertEquals(one, testAH.getShort());
                Assert.assertEquals(Short.valueOf(one), testAH.getShortObject());
*/
/*
                Assert.assertEquals(one, testAH.getShort2());
                Assert.assertEquals(Short.valueOf(one), testAH.getShortObject2());
*//*

            }

            {
                IAHFactory storage = new Storage(new QueryHelperStub((byte) 1));
                final ITByteAH testAH = storage.get(ITByteAH.class);
                Assert.assertEquals(one, testAH.getByte());
                Assert.assertEquals(Byte.valueOf(one), testAH.getByteObject());
*/
/*
                Assert.assertEquals(one, testAH.getByte2());
                Assert.assertEquals(Byte.valueOf(one), testAH.getByteObject2());
*//*

            }

            {
                IAHFactory storage = new Storage(new QueryHelperStub(true));
                final ITBooleanAH testAH = storage.get(ITBooleanAH.class);
                Assert.assertEquals(true, testAH.getBoolean());
                Assert.assertEquals(Boolean.TRUE, testAH.getBooleanObject());
*/
/*
                Assert.assertEquals(true, testAH.getBoolean2());
                Assert.assertEquals(Boolean.TRUE, testAH.getBooleanObject2());
*//*

            }

            {
                IAHFactory storage = new Storage(new QueryHelperStub(1.f));
                final ITFloatAH testAH = storage.get(ITFloatAH.class);
                Assert.assertEquals(1.f, testAH.getFloat(), 0);
                Assert.assertEquals(Float.valueOf(1.f), testAH.getFloatObject());
*/
/*
                Assert.assertEquals(1.f, testAH.getFloat2(), 0);
                Assert.assertEquals(Float.valueOf(1.f), testAH.getFloatObject2());
*//*

            }

            {
                IAHFactory storage = new Storage(new QueryHelperStub(1.));
                final ITDoubleAH testAH = storage.get(ITDoubleAH.class);
                Assert.assertEquals(1., testAH.getDouble(), 0.0000000001);
                Assert.assertEquals(Double.valueOf(1.), testAH.getDoubleObject());
*/
/*
                Assert.assertEquals(1., testAH.getDouble2(), 0.0000000001);
                Assert.assertEquals(Double.valueOf(1.), testAH.getDoubleObject2());
*//*

            }
        }
    }
       */
    @Test
    public void generateToObjConverter() throws StorageException {
        IAHFactory storage = new Storage(new ConnectionFactoryStub());

        storage.get(ITObjAH.class);

        try {
            storage.get(ITObjFail1AH.class);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(
                    "Exception occurs while building method public abstract org.xblackcat.sjpu.storage.impl.FailData org.xblackcat.sjpu.storage.impl.ITObjFail1AH.getException() throws org.xblackcat.sjpu.storage.StorageException: Can't find a way to convert result row to object. Probably one of the following annotations should be used: [interface org.xblackcat.sjpu.storage.ann.ToObjectConverter, interface org.xblackcat.sjpu.storage.ann.RowMap, interface org.xblackcat.sjpu.storage.ann.MapRowTo]",
                    e.getMessage()
            );
        }

        try {
            storage.get(ITObjFail2AH.class);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(
                    "Exception occurs while building method public abstract org.xblackcat.sjpu.storage.impl.NoDefaultData org.xblackcat.sjpu.storage.impl.ITObjFail2AH.getException() throws org.xblackcat.sjpu.storage.StorageException: Can't find a way to convert result row to object. Probably one of the following annotations should be used: [interface org.xblackcat.sjpu.storage.ann.ToObjectConverter, interface org.xblackcat.sjpu.storage.ann.RowMap, interface org.xblackcat.sjpu.storage.ann.MapRowTo]",
                    e.getMessage()
            );
        }

        try {
            storage.get(ITObjFail3AH.class);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(
                    "Method public abstract org.xblackcat.sjpu.storage.impl.NoDefaultData org.xblackcat.sjpu.storage.impl.ITObjFail3AH.getException() throws org.xblackcat.sjpu.storage.StorageException should meet only one of the following requirements: annotated with org.xblackcat.sjpu.storage.ann.Sql or annotated with org.xblackcat.sjpu.storage.ann.DDL",
                    e.getMessage()
            );
        }
    }

    private static class ConnectionFactoryStub implements IConnectionFactory {
        @Override
        public Connection getConnection() throws SQLException {
            return null;
        }

        @Override
        public void shutdown() throws StorageException {

        }
    }

    /*
    @Test
    public void generateComplexAH() throws StorageException {
        final byte one = (byte) 1;

        IAHFactory storage;
        IComplexAH testAH;

        storage = new Storage(new QueryHelperStub(1l));
        testAH = storage.get(IComplexAH.class);
        Assert.assertEquals(one, testAH.getLong());
        Assert.assertEquals(Long.valueOf(one), testAH.getLongObject());

        storage = new Storage(new QueryHelperStub(1));
        testAH = storage.get(IComplexAH.class);
        Assert.assertEquals(one, testAH.getInt());
        Assert.assertEquals(Integer.valueOf(one), testAH.getInteger());

        storage = new Storage(new QueryHelperStub((short) 1));
        testAH = storage.get(IComplexAH.class);
        Assert.assertEquals(one, testAH.getShort());
        Assert.assertEquals(Short.valueOf(one), testAH.getShortObject());

        storage = new Storage(new QueryHelperStub((byte) 1));
        testAH = storage.get(IComplexAH.class);
        Assert.assertEquals(one, testAH.getByte());
        Assert.assertEquals(Byte.valueOf(one), testAH.getByteObject());

        storage = new Storage(new QueryHelperStub(true));
        testAH = storage.get(IComplexAH.class);
        Assert.assertEquals(true, testAH.getBoolean());
        Assert.assertEquals(Boolean.TRUE, testAH.getBooleanObject());

        storage = new Storage(new QueryHelperStub(1.f));
        testAH = storage.get(IComplexAH.class);
        Assert.assertEquals(1.f, testAH.getFloat(), 0);
        Assert.assertEquals(Float.valueOf(1.f), testAH.getFloatObject());

        storage = new Storage(new QueryHelperStub(1.));
        testAH = storage.get(IComplexAH.class);
        Assert.assertEquals(1., testAH.getDouble(), 0.0000000001);
        Assert.assertEquals(Double.valueOf(1.), testAH.getDoubleObject());
    }
*/
}
