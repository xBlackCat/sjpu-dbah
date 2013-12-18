package org.xblackcat.sjpu.storage.impl;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.xblackcat.sjpu.storage.ATypeMap;
import org.xblackcat.sjpu.storage.StorageSetupException;

import java.util.HashMap;
import java.util.Map;

/**
 * 17.12.13 14:51
 *
 * @author xBlackCat
 */
class TypeMapper {
    private final Map<Class<?>, Class<? extends ATypeMap<?, ?>>> objectMappers = new HashMap<>();
    private final Map<Class<?>, Class<? extends ATypeMap<?, ?>>> interfaceMappers = new HashMap<>();

    @SafeVarargs
    public TypeMapper(Class<? extends ATypeMap<?, ?>>... mappers) {
        final ClassPool pool = ClassPool.getDefault();

        try {
            for (Class<? extends ATypeMap<?, ?>> m : mappers) {
                ATypeMap<?, ?> mapper = BuilderUtils.initializeMapper(pool, m);

                final Class<?> realType = mapper.getRealType();
                if (realType.isInterface()) {
                    interfaceMappers.put(realType, m);
                } else {
                    objectMappers.put(realType, m);
                }
            }
        } catch (ReflectiveOperationException | CannotCompileException | NotFoundException e) {
            throw new StorageSetupException("Can't initialize one of the mapper", e);
        }

    }

    public Class<? extends ATypeMap<?, ?>> hasTypeMap(Class<?> objClass) {
        Class<? extends ATypeMap<?, ?>> mapClass;
        if (!objClass.isInterface()) {
            mapClass = findThoughHierarchy(objClass);
        } else {
            mapClass = interfaceMappers.get(objClass);
        }

        if (mapClass != null) {
            return mapClass;
        }

        for (Map.Entry<Class<?>, Class<? extends ATypeMap<?, ?>>> m : interfaceMappers.entrySet()) {
            if (m.getKey().isAssignableFrom(objClass)) {
                return m.getValue();
            }
        }

        return null;
    }

    private Class<? extends ATypeMap<?, ?>> findThoughHierarchy(Class<?> obj) {
        if (obj == null) {
            return null;
        }

        final Class<? extends ATypeMap<?, ?>> mapClass = objectMappers.get(obj);
        if (mapClass == null) {
            return findThoughHierarchy(obj.getSuperclass());
        } else {
            return mapClass;
        }
    }
}
