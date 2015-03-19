package org.xblackcat.sjpu.storage.typemap;

/**
 * 19.12.13 17:00
 *
 * @author xBlackCat
 */
public interface IMapFactory<RealObject, DBObject> {
    boolean isAccepted(Class<?> obj);

    ITypeMap<RealObject, DBObject> mapper(Class<RealObject> clazz);
}
