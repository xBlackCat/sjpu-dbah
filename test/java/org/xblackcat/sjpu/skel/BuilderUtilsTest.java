package org.xblackcat.sjpu.skel;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;

/**
 * 20.10.2015 20:14
 *
 * @author xBlackCat
 */
public class BuilderUtilsTest {
    @Test
    public void simpleResolving() {
        final Map<TypeVariable<?>, Class<?>> typeVariables = BuilderUtils.resolveTypeVariables(SimpleTestClass1.class);

        Assert.assertEquals(
                Collections.singletonMap(
                        Callable.class.getTypeParameters()[0],
                        Integer.class
                ),
                typeVariables
        );
    }

    @Test
    public void complexResolving() throws ReflectiveOperationException {
        {
            final Map<TypeVariable<?>, Class<?>> typeVariables = BuilderUtils.resolveTypeVariables(TestSubSubClass1.class);

            final Map<TypeVariable<?>, Class<?>> expected = new HashMap<>();
            expected.put(Callable.class.getTypeParameters()[0], String.class);
            expected.put(TestClass1.class.getTypeParameters()[0], String.class);
            expected.put(TestClass1.class.getTypeParameters()[1], Double.class);
            expected.put(TestSubClass1.class.getTypeParameters()[0], String.class);

            Assert.assertEquals(expected, typeVariables);
        }

        {
            final Map<TypeVariable<?>, Class<?>> typeVariables = BuilderUtils.resolveTypeVariables(TestSubClass1.class);

            final Map<TypeVariable<?>, Class<?>> expected = new HashMap<>();
            expected.put(TestClass1.class.getTypeParameters()[1], Double.class);

            Assert.assertEquals(expected, typeVariables);
        }

        {
            final Method methodModel = getClass().getMethod("methodModel", TestClass1.class);
            final Type argType = methodModel.getGenericParameterTypes()[0];
            final Map<TypeVariable<?>, Class<?>> typeVariables = BuilderUtils.resolveTypeVariables(argType);

            final Map<TypeVariable<?>, Class<?>> expected = new HashMap<>();
            expected.put(Callable.class.getTypeParameters()[0], Long.class);
            expected.put(TestClass1.class.getTypeParameters()[0], Long.class);
            expected.put(TestClass1.class.getTypeParameters()[1], Character.class);

            Assert.assertEquals(expected, typeVariables);
        }
        {
            final Method methodModel = getClass().getMethod("methodModel2", TestClass1.class);

            {
                final Type argType = methodModel.getGenericParameterTypes()[0];
                final Map<TypeVariable<?>, Class<?>> typeVariables = BuilderUtils.resolveTypeVariables(argType);
                final Map<TypeVariable<?>, Class<?>> expected = new HashMap<>();
                expected.put(Callable.class.getTypeParameters()[0], Class.class);
                expected.put(TestClass1.class.getTypeParameters()[0], Class.class);
                expected.put(TestClass1.class.getTypeParameters()[1], Callable.class);

                Assert.assertEquals(expected, typeVariables);
            }

            {
                final Type argType = methodModel.getGenericReturnType();
                final Map<TypeVariable<?>, Class<?>> typeVariables = BuilderUtils.resolveTypeVariables(argType);

                final Map<TypeVariable<?>, Class<?>> expected = new HashMap<>();
                expected.put(Callable.class.getTypeParameters()[0], Thread.class);
                expected.put(TestClass1.class.getTypeParameters()[0], Thread.class);
                expected.put(TestClass1.class.getTypeParameters()[1], SortedMap.class);
                expected.put(TestSub2Class1.class.getTypeParameters()[0], Thread.class);
                expected.put(TestSub2Class1.class.getTypeParameters()[1], SortedMap.class);

                Assert.assertEquals(expected, typeVariables);
            }

        }
    }

    @SuppressWarnings("unused")
    public void methodModel(TestClass1<Long, Character> arg) {

    }

    @SuppressWarnings("unused")
    public TestSub2Class1<Thread, SortedMap<Thread, Integer>> methodModel2(TestClass1<Class<? extends Runnable>, Callable<Integer>> arg) {
        return null;
    }
}
