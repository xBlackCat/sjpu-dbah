package org.xblackcat.sjpu.storage.converter.builder;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.typemap.TypeMapTest;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Date;

/**
 * 25.04.2014 16:00
 *
 * @author xBlackCat
 */
public class AnalyzerTest {
    @Test
    public void testSimpleAnalyzer() throws NoSuchMethodException {
        final AnAnalyser analyzer = new DefaultAnalyzer(TypeMapTest.TEST_TYPE_MAPPER);

        {
            final Info info = analyzer.analyze(SimpleObject.class);

            Assert.assertEquals("", info.suffix);
            final Constructor<?>[] expected = {
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class)
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final Info info = analyzer.analyze(Complex1.class);

            Assert.assertEquals("", info.suffix);
            final Constructor<?>[] expected = {
                    Complex1.class.getConstructor(int.class, SimpleObject.class, String.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class)
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final Info info = analyzer.analyze(Complex2.class);

            Assert.assertEquals("Def", info.suffix);
            final Constructor<?>[] expected = {
                    Complex2.class.getConstructor(String.class, SimpleObject.class, long.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class)
            };
            Assert.assertArrayEquals(expected, info.reference);
        }
    }

    public void testSignatureAnalyzer() throws NoSuchMethodException {
        {
            final AnAnalyser analyzer = new SignatureFinder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    int.class,
                    String.class,
                    URI.class,
                    Date.class,
                    long.class
            );
            final Info info = analyzer.analyze(SimpleObject.class);

            Assert.assertEquals("1", info.suffix);
            final Constructor<?>[] expected = {
                    Complex2.class.getConstructor(int.class, SimpleObject.class, long.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class),
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final AnAnalyser analyzer = new SignatureFinder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    int.class,
                    String.class,
                    URI.class,
                    Date.class,
                    String.class,
                    URI.class,
                    Date.class
            );
            final Info info = analyzer.analyze(SimpleObject.class);

            Assert.assertEquals("3", info.suffix);
            final Constructor<?>[] expected = {
                    Complex2.class.getConstructor(int.class, SimpleObject.class, SimpleObject.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class),
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final AnAnalyser analyzer = new SignatureFinder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    int.class,
                    String.class,
                    URI.class,
                    Date.class,
                    long.class,
                    Date.class,
                    String.class,
                    URI.class,
                    Date.class
            );
            final Info info = analyzer.analyze(SimpleObject.class);

            Assert.assertEquals("1", info.suffix);
            final Constructor<?>[] expected = {
                    DeepComplex.class.getConstructor(Complex2.class, Date.class, SimpleObject.class),
                    Complex2.class.getConstructor(int.class, SimpleObject.class, SimpleObject.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, Date.class),
            };
            Assert.assertArrayEquals(expected, info.reference);
        }
    }
}
