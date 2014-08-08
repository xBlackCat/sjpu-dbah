package org.xblackcat.sjpu.storage.typemap;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.workflow.Numbers;

import java.sql.Timestamp;
import java.util.Date;


/**
 * 08.08.2014 10:54
 *
 * @author xBlackCat
 */
public class MapperTest {
    @Test
    public void testDateMapper() {
        final DateMapper mapper = new DateMapper();

        Assert.assertTrue(mapper.isAccepted(Date.class));
        Assert.assertFalse(mapper.isAccepted(java.sql.Date.class));
        Assert.assertFalse(mapper.isAccepted(java.sql.Timestamp.class));
        Assert.assertFalse(mapper.isAccepted(java.sql.Time.class));
        Assert.assertFalse(mapper.isAccepted(String.class));
        Assert.assertFalse(mapper.isAccepted(Object.class));

        final ITypeMap<Date, Timestamp> map = mapper.mapper(Date.class);

        Assert.assertEquals(Timestamp.class, map.getDbType());
        Assert.assertEquals(java.util.Date.class, map.getRealType());

        {
            final Date date = new Date(1407481451844l);
            final Timestamp timestamp = map.forStore(date);
            Assert.assertEquals(date, map.forRead(timestamp));
        }

        {
            final Timestamp timestamp = new Timestamp(1407481451844l);
            final Date date = map.forRead(timestamp);
            Assert.assertEquals(timestamp, map.forStore(date));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnumStringMapper() {
        EnumToStringMapper mapper = new EnumToStringMapper();

        Assert.assertTrue(mapper.isAccepted(Numbers.class));
        Assert.assertTrue(mapper.isAccepted(TestEnum.class));
        Assert.assertFalse(mapper.isAccepted(Enum.class));
        Assert.assertFalse(mapper.isAccepted(String.class));
        Assert.assertFalse(mapper.isAccepted(Object.class));

        {
            final ITypeMap<Numbers, String> map = mapper.mapper(Numbers.class);
            Assert.assertEquals(String.class, map.getDbType());
            Assert.assertEquals(Numbers.class, map.getRealType());

            {
                Numbers n = Numbers.Four;
                String value = map.forStore(n);
                Assert.assertEquals(n, map.forRead(value));
            }

            {
                String value = "Four";
                Numbers n = map.forRead(value);
                Assert.assertEquals(value, map.forStore(n));
            }
        }

        {
            final ITypeMap<TestEnum, String> map = mapper.mapper(TestEnum.class);
            Assert.assertEquals(String.class, map.getDbType());
            Assert.assertEquals(TestEnum.class, map.getRealType());

            {
                TestEnum n = TestEnum.Second;
                String value = map.forStore(n);
                Assert.assertEquals(n, map.forRead(value));
            }

            {
                String value = "Second";
                TestEnum n = map.forRead(value);
                Assert.assertEquals(value, map.forStore(n));
            }
        }
    }

    private static enum TestEnum {
        First,
        Second,
        Third
    }
}
