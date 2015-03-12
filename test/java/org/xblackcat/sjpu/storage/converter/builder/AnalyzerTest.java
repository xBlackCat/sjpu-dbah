package org.xblackcat.sjpu.storage.converter.builder;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.typemap.TypeMapTest;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.time.LocalDateTime;

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
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class)
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final Info info = analyzer.analyze(Complex1.class);

            Assert.assertEquals("", info.suffix);
            final Constructor<?>[] expected = {
                    Complex1.class.getConstructor(int.class, SimpleObject.class, String.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class)
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final Info info = analyzer.analyze(Complex2.class);

            Assert.assertEquals("Def", info.suffix);
            final Constructor<?>[] expected = {
                    Complex2.class.getConstructor(String.class, SimpleObject.class, long.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class)
            };
            Assert.assertArrayEquals(expected, info.reference);
        }
    }

    @Test
    public void testSignatureAnalyzer() throws NoSuchMethodException {
        {
            final AnAnalyser analyzer = new SignatureFinder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    int.class,
                    String.class,   // SimpleObject
                    URI.class,      //      |
                    LocalDateTime.class,     //      /
                    long.class
            );
            final Info info = analyzer.analyze(Complex2.class);

            final Constructor<?>[] expected = {
                    Complex2.class.getConstructor(int.class, SimpleObject.class, long.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class),
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final AnAnalyser analyzer = new SignatureFinder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    int.class,
                    String.class,  // SimpleObject
                    URI.class,     //      |
                    LocalDateTime.class,    // -----/
                    String.class,  // SimpleObject
                    URI.class,     //      |
                    LocalDateTime.class     // -----/
            );
            final Info info = analyzer.analyze(Complex2.class);

            final Constructor<?>[] expected = {
                    Complex2.class.getConstructor(int.class, SimpleObject.class, SimpleObject.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class),
            };
            Assert.assertArrayEquals(expected, info.reference);
        }

        {
            final AnAnalyser analyzer = new SignatureFinder(
                    TypeMapTest.TEST_TYPE_MAPPER,
                    int.class,     //  --------- Complex2
                    String.class,  //  SimpleObject    |
                    URI.class,     //      |           |
                    LocalDateTime.class,    //  ----/           |
                    long.class,    //  ----------------/
                    LocalDateTime.class,
                    String.class,  // SimpleObject
                    URI.class,     //      |
                    LocalDateTime.class     // -----/
            );
            final Info info = analyzer.analyze(DeepComplex.class);

            final Constructor<?>[] expected = {
                    DeepComplex.class.getConstructor(Complex2.class, LocalDateTime.class, SimpleObject.class),
                    Complex2.class.getConstructor(int.class, SimpleObject.class, long.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class),
                    SimpleObject.class.getConstructor(String.class, URI.class, LocalDateTime.class),
            };
            Assert.assertArrayEquals(expected, info.reference);
        }
    }
}
