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
        return new EnumStringATypeMap(clazz);
    }

    private static class EnumStringATypeMap extends ATypeMap<Enum, String> {
        public EnumStringATypeMap(Class<Enum> clazz) {
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
        public Enum forRead(String obj) {
            if (obj == null) {
                return null;
            }

            return Enum.valueOf(getRealType(), obj);
        }
    }
}
