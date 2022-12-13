package org.xblackcat.sjpu.storage.typemap;

import java.sql.Array;
import java.util.Arrays;

public class ArrayMapper<T> implements IMapFactory<T[], Array> {
    private final Class<T> componentClazz;
    private final ITypeMap<T[], Array> typeMap;

    @SuppressWarnings("unchecked")
    public ArrayMapper(Class<T> componentClazz, String componentSqlType) {
        this.componentClazz = componentClazz;
        final Class<T[]> arrayClazz = (Class<T[]>) java.lang.reflect.Array.newInstance(componentClazz, 0).getClass();
        typeMap = new NullPassTypeMap<>(
                arrayClazz,
                Array.class,
                (connection, values) -> connection.createArrayOf(componentSqlType, values),
                object -> {
                    final Object[] array = (Object[]) object.getArray();
                    return (T[]) Arrays.copyOf(array, array.length, arrayClazz);
                }
        );
    }

    @Override
    public boolean isAccepted(Class<?> obj) {
        return obj.isArray() && obj.getComponentType().equals(componentClazz);
    }

    @Override
    public ITypeMap<T[], Array> mapper(Class<T[]> clazz) {
        return typeMap;
    }
}