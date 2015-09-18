package org.xblackcat.sjpu.storage;

import org.xblackcat.sjpu.storage.typemap.IMapFactory;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;
import org.xblackcat.sjpu.storage.typemap.NullPassTypeMap;

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
        return new NullPassTypeMap<>(
                URI.class,
                byte[].class,
                uri -> uri.toString().getBytes(StandardCharsets.UTF_8),
                bytes -> URI.create(new String(bytes, StandardCharsets.UTF_8))
        );
    }
}
