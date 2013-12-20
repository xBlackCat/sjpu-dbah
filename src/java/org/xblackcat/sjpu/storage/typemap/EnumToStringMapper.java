package org.xblackcat.sjpu.storage.typemap;

/**
 * 20.12.13 12:35
 *
 * @author xBlackCat
 */
public class EnumToStringMapper implements IMapFactory<Enum, String> {
    @Override
    public boolean isAccepted(Class<?> obj) {
        return Enum.class.isAssignableFrom(obj);
    }

    @Override
    public ITypeMap<Enum, String> mapper(Class<Enum> clazz) {
        return new EnumStringTypeMap(clazz);
    }

}
