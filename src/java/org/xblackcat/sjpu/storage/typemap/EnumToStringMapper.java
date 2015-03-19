package org.xblackcat.sjpu.storage.typemap;

/**
 * 20.12.13 12:35
 *
 * @author xBlackCat
 */
public class EnumToStringMapper<T extends Enum<T>> implements IMapFactory<T, String> {
    @Override
    public boolean isAccepted(Class<?> obj) {
        return Enum.class.isAssignableFrom(obj) && !Enum.class.equals(obj);
    }

    @Override
    public ITypeMap<T, String> mapper(Class<T> clazz) {
        return new NullPassTypeMap<>(clazz, String.class, Enum::name, s -> Enum.valueOf(clazz, s));
    }
}
