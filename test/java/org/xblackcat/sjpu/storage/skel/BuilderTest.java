package org.xblackcat.sjpu.storage.skel;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.typemap.TypeMapTest;

/**
 * 25.04.2014 13:17
 *
 * @author xBlackCat
 */
public class BuilderTest {

    @Test
    public void bodyBuild() {
        {

            final ConverterBuilder builder = new ConverterBuilder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    SimpleObject.class.getConstructors()[0]
            );

            String expected = "{\n" +
                    "java.lang.String value1 = $1.getString(1);\n" +
                    "byte[] value2 = $1.getBytes(2);\n" +
                    "java.sql.Timestamp value3 = $1.getTimestamp(3);\n" +
                    "\n" +
                    "return new org.xblackcat.sjpu.storage.skel.SimpleObject(\n" +
                    "value1,\n" +
                    "(java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value2),\n" +
                    "(java.util.Date) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_util_Date_0_Instance.I.forRead(value3)\n" +
                    ");\n" +
                    "}";

            Assert.assertEquals(expected, builder.buildBody());
        }

        {
            final ConverterBuilder builder = new ConverterBuilder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    Complex1.class.getConstructors()[0],
                    SimpleObject.class.getConstructors()[0]
            );

            String expected = "{\n" +
                    "int value1 = $1.getInt(1);\n" +
                    "java.lang.String value2 = $1.getString(2);\n" +
                    "byte[] value3 = $1.getBytes(3);\n" +
                    "java.sql.Timestamp value4 = $1.getTimestamp(4);\n" +
                    "java.lang.String value5 = $1.getString(5);\n" +
                    "\n" +
                    "return new org.xblackcat.sjpu.storage.skel.Complex1(\n" +
                    "value1,\n" +
                    "new org.xblackcat.sjpu.storage.skel.SimpleObject(\n" +
                    "value2,\n" +
                    "(java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value3),\n" +
                    "(java.util.Date) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_util_Date_0_Instance.I.forRead(value4)\n" +
                    "),\n" +
                    "value5\n" +
                    ");\n" +
                    "}";

            Assert.assertEquals(expected, builder.buildBody());
        }
    }

    @Test
    public void failedMapping() {
        final ConverterBuilder builder = new ConverterBuilder(
                TypeMapTest.TEST_TYPE_MAPPER,
                Complex1.class.getConstructors()[0]
        );

        try {
            builder.buildBody();
            Assert.fail("Exception expected");
        } catch (StorageSetupException e) {
            // Ignore
        }
    }
}
