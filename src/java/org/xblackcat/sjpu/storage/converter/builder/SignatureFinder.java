package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.RowMap;
import org.xblackcat.sjpu.storage.ann.ToObjectConverter;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * 25.04.2014 15:32
 *
 * @author xBlackCat
 */
class SignatureFinder extends AnAnalyser {
    private final Class<?>[] signature;

    public SignatureFinder(TypeMapper typeMapper, Class<?>... signature) {
        super(typeMapper);
        this.signature = signature;
    }

    private Integer isMatched(Constructor<?> c, int shift, Deque<Constructor<?>> stack) {
        final Class<?>[] params = c.getParameterTypes();
        if (params.length == 0) {
            // Default constructors are not an option
            return null;
        }

        if (params.length + shift > signature.length) {
            // Not matched
            return null;
        }

        int amount = 0;
        for (Class<?> param : params) {
            if (canProcess(param)) {
                if (!signature[amount + shift].isAssignableFrom(param)) {
                    return null;
                }
                amount++;
            } else {
                Integer subElement = isMatched(param, shift + amount, stack);

                if (subElement == null) {
                    return null;
                }

                amount += subElement;
            }
        }

        return amount;
    }

    private Integer isMatched(Class<?> param, int shift, Deque<Constructor<?>> stack) {
        for (Constructor<?> c : param.getConstructors()) {
            stack.addLast(c);
            Integer processed = isMatched(c, shift, stack);
            if (processed != null) {
                return processed;
            }
            stack.removeLast();
        }

        return null;
    }

    @Override
    public Info analyze(Class<?> clazz) {
        Deque<Constructor<?>> stack = new ArrayDeque<>();

        final Integer matched = isMatched(clazz, 0, stack);

        if (matched == null || stack.isEmpty()) {
            throw new StorageSetupException(
                    "Can't find a way to convert result row to object. Probably one of the following annotations should be used: " +
                            Arrays.asList(ToObjectConverter.class, RowMap.class, MapRowTo.class)
            );
        }


        final Constructor<?>[] reference = stack.toArray(new Constructor<?>[stack.size()]);

        Constructor<?>[] constructors = clazz.getConstructors();
        int i = 0;
        while (i < constructors.length) {
            Constructor<?> c = constructors[i];

            if (c.equals(reference[0])) {
                return new Info(String.valueOf(i), reference);
            }
            i++;
        }

        throw new StorageSetupException("Unexpected state: already found constructor is gone with the wind");
    }
}
