package org.xblackcat.sjpu.storage.converter.builder;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xblackcat.sjpu.builder.GeneratorException;
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

            String expected = """
                    {
                    java.lang.String value1 = $1.getString(1);
                    byte[] value2 = $1.getBytes(2);
                    java.sql.Timestamp value3 = $1.getTimestamp(3);

                    return new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(
                    value1,
                    (java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value2),
                    (java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value3)
                    );
                    }""";

            Assertions.assertEquals(expected, builder.buildBody());
        }

        {
            final ConverterMethodBuilder builder = new ConverterMethodBuilder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    Complex1.class.getConstructors()[0],
                    SimpleObject.class.getConstructors()[0]
            );

            String expected = """
                    {
                    int value1 = $1.getInt(1);
                    java.lang.String value2 = $1.getString(2);
                    byte[] value3 = $1.getBytes(3);
                    java.sql.Timestamp value4 = $1.getTimestamp(4);
                    java.lang.String value5 = $1.getString(5);

                    return new org.xblackcat.sjpu.storage.converter.builder.Complex1(
                    value1,
                    new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(
                    value2,
                    (java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value3),
                    (java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value4)
                    ),
                    value5
                    );
                    }""";

            Assertions.assertEquals(expected, builder.buildBody());
        }
    }


    @Test
    public void autoBodyBuild() throws NotFoundException, CannotCompileException {
        {
            final String body = build(TypeMapTest.TEST_TYPE_MAPPER, SimpleObject.class, null);

            String expected = """
                    {
                    java.lang.String value1 = $1.getString(1);
                    byte[] value2 = $1.getBytes(2);
                    java.sql.Timestamp value3 = $1.getTimestamp(3);

                    return new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(
                    value1,
                    (java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value2),
                    (java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value3)
                    );
                    }""";

            Assertions.assertEquals(expected, body);
        }

        {
            final String body = build(TypeMapTest.TEST_TYPE_MAPPER, Complex1.class, null);

            String expected = """
                    {
                    int value1 = $1.getInt(1);
                    java.lang.String value2 = $1.getString(2);
                    byte[] value3 = $1.getBytes(3);
                    java.sql.Timestamp value4 = $1.getTimestamp(4);
                    java.lang.String value5 = $1.getString(5);

                    return new org.xblackcat.sjpu.storage.converter.builder.Complex1(
                    value1,
                    new org.xblackcat.sjpu.storage.converter.builder.SimpleObject(
                    value2,
                    (java.net.URI) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_net_URI_0_Instance.I.forRead(value3),
                    (java.time.LocalDateTime) org.xblackcat.sjpu.storage.typemap.TypeMapper.TypeMap_java_time_LocalDateTime_0_Instance.I.forRead(value4)
                    ),
                    value5
                    );
                    }""";

            Assertions.assertEquals(expected, body);
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

        final ConverterMethodBuilder builder = new ConverterMethodBuilder(typeMapper, info.reference());
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
            Assertions.fail("Exception expected");
        } catch (GeneratorException e) {
            Assertions.assertEquals("Can't process type org.xblackcat.sjpu.storage.converter.builder.SimpleObject", e.getMessage());
        }
    }
}
