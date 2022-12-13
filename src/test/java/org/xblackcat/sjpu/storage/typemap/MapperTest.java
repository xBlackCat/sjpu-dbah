package org.xblackcat.sjpu.storage.typemap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xblackcat.sjpu.storage.workflow.data.Numbers;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;


/**
 * 08.08.2014 10:54
 *
 * @author xBlackCat
 */
@SuppressWarnings("deprecation")
public class MapperTest {
    @Test
    public void testDateMapper() throws SQLException {
        final DateMapper mapper = new DateMapper();

        Assertions.assertTrue(mapper.isAccepted(Date.class));
        Assertions.assertFalse(mapper.isAccepted(java.sql.Date.class));
        Assertions.assertFalse(mapper.isAccepted(Timestamp.class));
        Assertions.assertFalse(mapper.isAccepted(Time.class));
        Assertions.assertFalse(mapper.isAccepted(String.class));
        Assertions.assertFalse(mapper.isAccepted(Object.class));

        final ITypeMap<Date, Timestamp> map = mapper.mapper(Date.class);

        Assertions.assertEquals(Timestamp.class, map.getDbType());
        Assertions.assertEquals(Date.class, map.getRealType());

        {
            final Date date = new Date(1407481451844L);
            final Timestamp timestamp = map.forStore(null, date);
            Assertions.assertEquals(date, map.forRead(timestamp));
        }

        {
            final Timestamp timestamp = new Timestamp(1407481451844L);
            final Date date = map.forRead(timestamp);
            Assertions.assertEquals(timestamp, map.forStore(null, date));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testEnumStringMapper() throws SQLException {
        EnumToStringMapper mapper = new EnumToStringMapper();

        Assertions.assertTrue(mapper.isAccepted(Numbers.class));
        Assertions.assertTrue(mapper.isAccepted(TestEnum.class));
        Assertions.assertFalse(mapper.isAccepted(Enum.class));
        Assertions.assertFalse(mapper.isAccepted(String.class));
        Assertions.assertFalse(mapper.isAccepted(Object.class));

        {
            final ITypeMap<Numbers, String> map = mapper.mapper(Numbers.class);
            Assertions.assertEquals(String.class, map.getDbType());
            Assertions.assertEquals(Numbers.class, map.getRealType());

            {
                Numbers n = Numbers.Four;
                String value = map.forStore(null, n);
                Assertions.assertEquals(n, map.forRead(value));
            }

            {
                String value = "Four";
                Numbers n = map.forRead(value);
                Assertions.assertEquals(value, map.forStore(null, n));
            }
        }

        {
            final ITypeMap<TestEnum, String> map = mapper.mapper(TestEnum.class);
            Assertions.assertEquals(String.class, map.getDbType());
            Assertions.assertEquals(TestEnum.class, map.getRealType());

            {
                TestEnum n = TestEnum.Second;
                String value = map.forStore(null, n);
                Assertions.assertEquals(n, map.forRead(value));
            }

            {
                String value = "Second";
                TestEnum n = map.forRead(value);
                Assertions.assertEquals(value, map.forStore(null, n));
            }
        }
    }

    private enum TestEnum {
        First,
        Second,
        Third
    }
}
