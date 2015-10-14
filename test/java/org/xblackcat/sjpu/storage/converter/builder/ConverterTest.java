package org.xblackcat.sjpu.storage.converter.builder;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xblackcat.sjpu.storage.typemap.TypeMapTest;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 14.10.2015 14:11
 *
 * @author xBlackCat
 */
@RunWith(Parameterized.class)
public class ConverterTest {
    @Parameterized.Parameters(name = "Check method {0} of class IConverterToys")
    public static Collection<Object[]> data() {
        return Stream.of(IConverterToys.class.getMethods()).map(m -> new Object[]{m}).collect(Collectors.toList());
    }

    private final Method testingMethod;

    public ConverterTest(Method testingMethod) {
        this.testingMethod = testingMethod;
    }

    @Test
    public void performAnalyse() throws CannotCompileException, ReflectiveOperationException, NotFoundException {
        final ConverterInfo info = ConverterInfo.analyse(TypeMapTest.TEST_TYPE_MAPPER, Collections.emptyMap(), testingMethod);

        Assert.assertNotNull(info);
    }
}
