package org.xblackcat.sjpu.storage.impl;


/**
 * 16.08.13 14:04
 *
 * @author xBlackCat
 */
public class AHGeneratorTest {
/*
    @Test
    public void checkPrimitiveAHStructure() throws StorageException {
        IAHFactory storage = new Storage(new QueryHelperStub(1, 2, 3));

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

    @Test
    public void generateToObjConverter() throws StorageException {
        IAHFactory storage = new Storage(new QueryHelperStub("1"));

        storage.get(ITObjAH.class);

        try {
            storage.get(ITObjFail1AH.class);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            storage.get(ITObjFail2AH.class);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            storage.get(ITObjFail3AH.class);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
