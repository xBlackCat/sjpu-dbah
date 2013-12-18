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
    private final Map<Class<?>, ATypeMap<?, ?>> objectMappers = new HashMap<>();
    private final Map<Class<?>, ATypeMap<?, ?>> interfaceMappers = new HashMap<>();

    @SafeVarargs
    public TypeMapper(Class<? extends ATypeMap<?, ?>>... mappers) {
        final ClassPool pool = ClassPool.getDefault();

        try {
            for (Class<? extends ATypeMap<?, ?>> m : mappers) {
                ATypeMap<?, ?> mapper = BuilderUtils.initializeMapper(pool, m);

                final Class<?> realType = mapper.getRealType();
                if (realType.isInterface()) {
                    interfaceMappers.put(realType, mapper);
                } else {
                    objectMappers.put(realType, mapper);
                }
            }
        } catch (ReflectiveOperationException | CannotCompileException | NotFoundException e) {
            throw new StorageSetupException("Can't initialize one of the mapper", e);
        }

    }

    public ATypeMap<?, ?> hasTypeMap(Class<?> objClass) {
        ATypeMap<?, ?> mapClass;
        if (!objClass.isInterface()) {
            mapClass = findThoughHierarchy(objClass);
        } else {
            mapClass = interfaceMappers.get(objClass);
        }

        if (mapClass != null) {
            return mapClass;
        }

        for (Map.Entry<Class<?>, ATypeMap<?, ?>> m : interfaceMappers.entrySet()) {
            if (m.getKey().isAssignableFrom(objClass)) {
                return m.getValue();
            }
        }

        return null;
    }

    private ATypeMap<?, ?> findThoughHierarchy(Class<?> obj) {
        if (obj == null) {
            return null;
        }

        final ATypeMap<?, ?> mapClass = objectMappers.get(obj);
        if (mapClass == null) {
            return findThoughHierarchy(obj.getSuperclass());
        } else {
            return mapClass;
        }
    }
}
