package org.xblackcat.sjpu.storage.typemap;

import javassist.ClassPool;
import org.junit.Test;
import org.xblackcat.sjpu.storage.UriTypeMap;

/**
 * 25.04.2014 14:52
 *
 * @author xBlackCat
 */
public class TypeMapTest {
    public static final TypeMapper TEST_TYPE_MAPPER = new TypeMapper(
            new ClassPool(true),
            0,
            new DateMapper(),
            new EnumToStringMapper(),
            new UriTypeMap()
    );

    @Test
    public void blank() {
    }
}
