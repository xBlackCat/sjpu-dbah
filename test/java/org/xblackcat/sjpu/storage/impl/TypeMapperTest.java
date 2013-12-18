package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.ATypeMap;
import org.xblackcat.sjpu.storage.IAH;

import java.net.URI;
import java.net.URL;

/**
 * 17.12.13 15:44
 *
 * @author xBlackCat
 */
public class TypeMapperTest {
    @Test
    public void testObjectMapper() {
        final TypeMapper mapper = new TypeMapper(Map1.class, Map2.class, Map3.class);

        Assert.assertEquals(Map1.class, mapper.hasTypeMap(URL.class));
        Assert.assertEquals(Map2.class, mapper.hasTypeMap(Integer.class));
        Assert.assertEquals(Map2.class, mapper.hasTypeMap(Double.class));
        Assert.assertEquals(Map3.class, mapper.hasTypeMap(AnAH.class));
        Assert.assertEquals(Map3.class, mapper.hasTypeMap(ITestBooleanAH.class));
    }


    private static class Map1 extends ATypeMap<URL, URI> {
        public Map1() {
            super(URL.class, URI.class);
        }

        @Override
        public URI forStore(URL obj) {
            return null;
        }

        @Override
        public URL forRead(URI obj) {
            return null;
        }
    }

    private static class Map2 extends ATypeMap<Number, Number> {
        public Map2() {
            super(Number.class, Number.class);
        }

        @Override
        public Number forStore(Number obj) {
            return null;
        }

        @Override
        public Number forRead(Number obj) {
            return null;
        }
    }

    private static class Map3 extends ATypeMap<IAH, IAH> {
        public Map3() {
            super(IAH.class, IAH.class);
        }

        @Override
        public IAH forStore(IAH obj) {
            return null;
        }

        @Override
        public IAH forRead(IAH obj) {
            return null;
        }
    }
}
