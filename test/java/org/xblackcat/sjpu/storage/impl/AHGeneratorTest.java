package org.xblackcat.sjpu.storage.impl;


import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.IStorage;
import org.xblackcat.sjpu.storage.StorageException;

import java.lang.reflect.Method;
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
    public void checkPrimitiveAHStructure() throws StorageException {
        IStorage storage = new Storage(new QueryHelperStub(1, 2, 3));

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
    public void generatePrimitiveAH() throws StorageException {
        {
            final byte one = (byte) 1;

            {
                IStorage storage = new Storage(new QueryHelperStub(1l, 2l, 3l));
                final ITestLongAH testAH = storage.get(ITestLongAH.class);
                Assert.assertEquals(one, testAH.getLong());
                Assert.assertEquals(Long.valueOf(one), testAH.getLongObject());
                Assert.assertEquals(one, testAH.getLong2());
                Assert.assertEquals(Long.valueOf(one), testAH.getLongObject2());
            }

            {
                IStorage storage = new Storage(new QueryHelperStub(1, 2, 3));
                final ITestIntAH testAH = storage.get(ITestIntAH.class);
                Assert.assertEquals(one, testAH.getInt());
                Assert.assertEquals(Integer.valueOf(one), testAH.getInteger());
                Assert.assertEquals(one, testAH.getInt2());
                Assert.assertEquals(Integer.valueOf(one), testAH.getInteger2());
            }

            {
                IStorage storage = new Storage(new QueryHelperStub((short) 1, (short) 2, (short) 3));
                final ITestShortAH testAH = storage.get(ITestShortAH.class);
                Assert.assertEquals(one, testAH.getShort());
                Assert.assertEquals(Short.valueOf(one), testAH.getShortObject());
                Assert.assertEquals(one, testAH.getShort2());
                Assert.assertEquals(Short.valueOf(one), testAH.getShortObject2());
            }

            {
                IStorage storage = new Storage(new QueryHelperStub((byte) 1, (byte) 2, (byte) 3));
                final ITestByteAH testAH = storage.get(ITestByteAH.class);
                Assert.assertEquals(one, testAH.getByte());
                Assert.assertEquals(Byte.valueOf(one), testAH.getByteObject());
                Assert.assertEquals(one, testAH.getByte2());
                Assert.assertEquals(Byte.valueOf(one), testAH.getByteObject2());
            }

            {
                IStorage storage = new Storage(new QueryHelperStub(true, false, true));
                final ITestBooleanAH testAH = storage.get(ITestBooleanAH.class);
                Assert.assertEquals(true, testAH.getBoolean());
                Assert.assertEquals(Boolean.TRUE, testAH.getBooleanObject());
                Assert.assertEquals(true, testAH.getBoolean2());
                Assert.assertEquals(Boolean.TRUE, testAH.getBooleanObject2());
            }

            {
                IStorage storage = new Storage(new QueryHelperStub(1.f, 2.f, 3.f));
                final ITestFloatAH testAH = storage.get(ITestFloatAH.class);
                Assert.assertEquals(1.f, testAH.getFloat(), 0);
                Assert.assertEquals(Float.valueOf(1.f), testAH.getFloatObject());
                Assert.assertEquals(1.f, testAH.getFloat2(), 0);
                Assert.assertEquals(Float.valueOf(1.f), testAH.getFloatObject2());
            }

            {
                IStorage storage = new Storage(new QueryHelperStub(1., 2., 3.));
                final ITestDoubleAH testAH = storage.get(ITestDoubleAH.class);
                Assert.assertEquals(1., testAH.getDouble(), 0.0000000001);
                Assert.assertEquals(Double.valueOf(1.), testAH.getDoubleObject());
                Assert.assertEquals(1., testAH.getDouble2(), 0.0000000001);
                Assert.assertEquals(Double.valueOf(1.), testAH.getDoubleObject2());
            }
        }
    }
}
