package org.xblackcat.sjpu.storage.typemap;

/**
 * 20.12.13 12:35
 *
 * @author xBlackCat
 */
public class EnumToStringMapper<T extends Enum<T>> implements IMapFactory<T, String> {
    @Override
    public boolean isAccepted(Class<?> obj) {
        return Enum.class.isAssignableFrom(obj);
    }

    @Override
    public ITypeMap<T, String> mapper(Class<T> clazz) {
        return new EnumStringATypeMap(clazz);
    }

    private class EnumStringATypeMap extends ATypeMap<T, String> {
        public EnumStringATypeMap(Class<T> clazz) {
            super(clazz, String.class);
        }

        @Override
        public String forStore(Enum obj) {
            if (obj == null) {
                return null;
            }

            return obj.name();
        }

        @Override
        public T forRead(String obj) {
            if (obj == null) {
                return null;
            }

            return Enum.valueOf(getRealType(), obj);
        }
    }
}
