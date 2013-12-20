package org.xblackcat.sjpu.storage.typemap;

/**
 * 20.12.13 12:44
 *
 * @author xBlackCat
 */
class EnumStringTypeMap extends ATypeMap<Enum, String> {
    EnumStringTypeMap(Class<Enum> clazz) {
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
