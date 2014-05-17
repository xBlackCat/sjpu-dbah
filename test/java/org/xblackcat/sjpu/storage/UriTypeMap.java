package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.typemap.ATypeMap;
import org.xblackcat.sjpu.storage.typemap.IMapFactory;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * 23.12.13 12:23
 *
 * @author xBlackCat
 */
public class UriTypeMap implements IMapFactory<URI, byte[]> {
    @Override
    public boolean isAccepted(Class<?> obj) {
        return URI.class.isAssignableFrom(obj);
    }

    @Override
    public ITypeMap<URI, byte[]> mapper(Class<URI> clazz) {
        return new ATypeMap<URI, byte[]>(URI.class, byte[].class) {
            @Override
            public byte[] forStore(URI obj) {
                if (obj == null) {
                    return null;
                }

                return obj.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public URI forRead(byte[] obj) {
                if (obj == null) {
                    return null;
                }

                return URI.create(new String(obj, StandardCharsets.UTF_8));
            }
        };
    }
}
