package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.ann.DefaultRowMap;
import org.xblackcat.sjpu.storage.ann.MapRowTo;
import org.xblackcat.sjpu.storage.ann.RowMap;
import org.xblackcat.sjpu.storage.ann.ToObjectConverter;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class DefaultAnalyzer extends AnAnalyser {
    DefaultAnalyzer(TypeMapper typeMapper) {
        super(typeMapper);
    }

    @Override
    public Info analyze(Class<?> clazz) {
        List<Constructor<?>> signature = new ArrayList<>();

        String suffix = checkParameters(clazz, signature);

        return new Info(suffix, signature.toArray(new Constructor<?>[signature.size()]));
    }

    private String checkParameters(Class<?> clazz, List<Constructor<?>> signature) {
        final Constructor<?>[] constructors = clazz.getConstructors();

        final String suffix;
        final Constructor<?> targetConstructor;

        if (constructors.length == 0) {
            throw new GeneratorException("Return object " + clazz + " has no public constructors");
        } else if (constructors.length == 1) {
            targetConstructor = constructors[0];
            suffix = "";
        } else {
            Constructor<?> def = null;

            int i = 0;
            int constructorsLength = constructors.length;

            while (i < constructorsLength) {
                Constructor<?> c = constructors[i];
                if (c.getAnnotation(DefaultRowMap.class) != null) {
                    def = c;
                    // No annotations so just use the constructor annotated as default map
                    break;
                }
                i++;
            }

            targetConstructor = def;
            suffix = "Def";
        }

        if (targetConstructor == null) {
            throw new GeneratorException(
                    "Can't find a way to convert result row to object. Probably one of the following annotations should be used: " +
                            Arrays.asList(ToObjectConverter.class, RowMap.class, MapRowTo.class)
            );
        }

        signature.add(targetConstructor);

        for (Class<?> param : targetConstructor.getParameterTypes()) {
            if (!canProcess(param)) {
                checkParameters(param, signature);
            }
        }

        return suffix;
    }
}
