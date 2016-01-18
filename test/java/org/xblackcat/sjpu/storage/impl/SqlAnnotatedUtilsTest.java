package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.converter.builder.Arg;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 30.06.2014 0:26
 *
 * @author xBlackCat
 */
public class SqlAnnotatedUtilsTest {
    @Test
    public void substitution() {
        {
            Collection<Arg> args = Arrays.asList(
                    new Arg(long.class, 0),
                    new Arg(short.class, 2),
                    new Arg(byte.class, 3)
            );
            List<Arg> opts = Arrays.asList(new Arg(null, 1, true), null, null, new Arg(null, 1, true));
            final Collection<Arg> actual = SqlAnnotatedBuilder.substituteOptionalArgs(
                    args,
                    opts,
                    long.class,
                    int.class,
                    short.class,
                    byte.class
            );

            Collection<Arg> expected = Arrays.asList(
                    new Arg(int.class, 1, true),
                    new Arg(long.class, 0),
                    new Arg(short.class, 2),
                    new Arg(int.class, 1, true),
                    new Arg(byte.class, 3)
            );

            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public <T> void substitution2() {
        {
            Collection<Arg> args = Arrays.asList(
                    new Arg(long.class, 0),
                    new Arg(short.class, 2),
                    new Arg(byte.class, 4)
            );
            List<Arg> opts = Arrays.asList(
                    new Arg(null, 1, true),
                    null,
                    new Arg(null, 3, false),
                    new Arg(null, 3, false),
                    null,
                    new Arg(null, 1, true),
                    new Arg(null, 3, false)
            );
            final Collection<Arg> actual = SqlAnnotatedBuilder.substituteOptionalArgs(
                    args,
                    opts,
                    long.class,
                    int.class,
                    short.class,
                    Double.class,
                    byte.class
            );

            Collection<Arg> expected = Arrays.asList(
                    new Arg(int.class, 1, true),
                    new Arg(long.class, 0),
                    new Arg(Double.class, 3),
                    new Arg(Double.class, 3),
                    new Arg(short.class, 2),
                    new Arg(int.class, 1, true),
                    new Arg(Double.class, 3),
                    new Arg(byte.class, 4)
            );

            Assert.assertEquals(expected, actual);
        }
    }
}
