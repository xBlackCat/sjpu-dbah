package org.xblackcat.sjpu.storage.converter.builder;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xblackcat.sjpu.storage.typemap.TypeMapTest;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.stream.Stream;

public class ConverterTest {
    public static Stream<Object[]> data() {
        return Stream.of(IConverterToys.class.getMethods()).map(m -> new Object[]{m});
    }

    @ParameterizedTest(name = "Check method {0} of class IConverterToys")
    @MethodSource("data")
    public void performAnalyse(Method testingMethod) throws CannotCompileException, ReflectiveOperationException, NotFoundException {
        final ConverterInfo info = ConverterInfo.analyse(TypeMapTest.TEST_TYPE_MAPPER, Collections.emptyMap(), testingMethod);

        Assertions.assertNotNull(info);
    }
}
