package org.xblackcat.sjpu.storage.converter.builder;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.typemap.TypeMapTest;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

/**
 * 25.04.2014 13:17
 *
 * @author xBlackCat
 */
public class BuilderTest {

    @Test
    public void bodyBuild() {
        {
            final ConverterMethodBuilder builder = new ConverterMethodBuilder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    SimpleObject.class.getConstructors()[0]
            );

            String expected = "{\n" +
                    "java.lang.String value1 = $1.getString(1);\n" +
                    "byte[] value2 = $1.getBytes(2);\n" +
                    "java.sql.Timestamp value3 = $1.getTimestamp(3);\n" +
                    "\n" +
                    "return new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(\n" +
                    "value1,\n" +
                    "(java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value2),\n" +
                    "(java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value3)\n" +
                    ");\n" +
                    "}";

            Assert.assertEquals(expected, builder.buildBody());
        }

        {
            final ConverterMethodBuilder builder = new ConverterMethodBuilder(
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
                    "return new org.xblackcat.sjpu.storage.converter.builder.Complex1(\n" +
                    "value1,\n" +
                    "new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(\n" +
                    "value2,\n" +
                    "(java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value3),\n" +
                    "(java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value4)\n" +
                    "),\n" +
                    "value5\n" +
                    ");\n" +
                    "}";

            Assert.assertEquals(expected, builder.buildBody());
        }
    }


    @Test
    public void autoBodyBuild() throws NotFoundException, CannotCompileException {
        {
            final String body = build(TypeMapTest.TEST_TYPE_MAPPER, SimpleObject.class, null);

            String expected = "{\n" +
                    "java.lang.String value1 = $1.getString(1);\n" +
                    "byte[] value2 = $1.getBytes(2);\n" +
                    "java.sql.Timestamp value3 = $1.getTimestamp(3);\n" +
                    "\n" +
                    "return new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(\n" +
                    "value1,\n" +
                    "(java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value2),\n" +
                    "(java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value3)\n" +
                    ");\n" +
                    "}";

            Assert.assertEquals(expected, body);
        }

        {
            final String body = build(TypeMapTest.TEST_TYPE_MAPPER, Complex1.class, null);

            String expected = "{\n" +
                    "int value1 = $1.getInt(1);\n" +
                    "java.lang.String value2 = $1.getString(2);\n" +
                    "byte[] value3 = $1.getBytes(3);\n" +
                    "java.sql.Timestamp value4 = $1.getTimestamp(4);\n" +
                    "java.lang.String value5 = $1.getString(5);\n" +
                    "\n" +
                    "return new org.xblackcat.sjpu.storage.converter.builder.Complex1(\n" +
                    "value1,\n" +
                    "new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(\n" +
                    "value2,\n" +
                    "(java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value3),\n" +
                    "(java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value4)\n" +
                    "),\n" +
                    "value5\n" +
                    ");\n" +
                    "}";

            Assert.assertEquals(expected, body);
        }
    }

    private static String build(TypeMapper typeMapper, Class<?> clazz, Class<?>[] signature) {
        final AnAnalyser analyser;
        if (signature == null) {
            analyser = new DefaultAnalyzer(typeMapper);
        } else {
            analyser = new SignatureFinder(typeMapper, signature);
        }

        final Info info = analyser.analyze(clazz);

        final ConverterMethodBuilder builder = new ConverterMethodBuilder(typeMapper, info.reference);
        return builder.buildBody();
    }


    @Test
    public void failedMapping() {
        final ConverterMethodBuilder builder = new ConverterMethodBuilder(
                TypeMapTest.TEST_TYPE_MAPPER,
                Complex1.class.getConstructors()[0]
        );

        try {
            builder.buildBody();
            Assert.fail("Exception expected");
        } catch (GeneratorException e) {
            e.printStackTrace();
            // Ignore
        }
    }
}
