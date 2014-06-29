package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;

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
            Collection<ConverterInfo.Arg> args = Arrays.asList(
                    new ConverterInfo.Arg(long.class, 0),
                    new ConverterInfo.Arg(short.class, 2),
                    new ConverterInfo.Arg(byte.class, 3)
            );
            List<Integer> opts = Arrays.asList(1, null, null, 1);
            final Collection<ConverterInfo.Arg> actual = SqlAnnotatedBuilder.substituteOptionalArgs(
                    args,
                    opts,
                    long.class,
                    int.class,
                    short.class,
                    byte.class
            );

            Collection<ConverterInfo.Arg> expected = Arrays.asList(
                    new ConverterInfo.Arg(int.class, 1, true),
                    new ConverterInfo.Arg(long.class, 0),
                    new ConverterInfo.Arg(short.class, 2),
                    new ConverterInfo.Arg(int.class, 1, true),
                    new ConverterInfo.Arg(byte.class, 3)
            );

            Assert.assertEquals(expected, actual);
        }
    }
}
