package org.xblackcat.sjpu.storage.impl;

import javassist.ClassPool;
import org.apache.commons.lang3.StringUtils;
import org.xblackcat.sjpu.storage.typemap.IMapFactory;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 17.12.13 14:51
 *
 * @author xBlackCat
 */
class TypeMapper {
    private final IMapFactory[] mappers;
    private final String suffix = Integer.toHexString(hashCode());

    private final Map<Class<?>, ITypeMap<?, ?>> initializedMappers = new HashMap<>();

    public TypeMapper(IMapFactory<?, ?>... mappers) {
        final ClassPool pool = ClassPool.getDefault();

        this.mappers = mappers;
    }

    public ITypeMap<?, ?> hasTypeMap(Class<?> objClass) {
        if (initializedMappers.containsKey(objClass)) {
            // Already checked classes are here with 'null' as type mappers
            return initializedMappers.get(objClass);
        }


        return null;
    }

    public static String getTypeMapperRef(TypeMapper mapper, Class<?> clazz) {
        return BuilderUtils.getName(TypeMapper.class) + "." + nestedClassName(mapper, clazz) + ".I";
    }

    private static String nestedClassName(TypeMapper mapper, Class<?> clazz) {
        return "_" + mapper.suffix + "_" + StringUtils.replaceChars(clazz.getName(), "$.[]", "____");
    }
}
