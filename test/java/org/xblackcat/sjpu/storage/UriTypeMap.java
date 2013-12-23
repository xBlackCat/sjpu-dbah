package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.typemap.ATypeMap;
import org.xblackcat.sjpu.storage.typemap.IMapFactory;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.net.URI;

/**
 * 23.12.13 12:23
 *
 * @author xBlackCat
 */
public class UriTypeMap implements IMapFactory<URI, String> {
    @Override
    public boolean isAccepted(Class<?> obj) {
        return URI.class.isAssignableFrom(obj);
    }

    @Override
    public ITypeMap<URI, String> mapper(Class<URI> clazz) {
        return new ATypeMap<URI, String>(URI.class, String.class) {
            @Override
            public String forStore(URI obj) {
                if (obj == null) {
                    return null;
                }

                return obj.toString();
            }

            @Override
            public URI forRead(String obj) {
                if (obj == null) {
                    return null;
                }

                return URI.create(obj);
            }
        };
    }
}
