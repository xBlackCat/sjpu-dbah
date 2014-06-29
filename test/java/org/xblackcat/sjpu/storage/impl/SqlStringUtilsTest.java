package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;

import java.util.Collections;
import java.util.Map;

/**
 * 14.06.2014 23:58
 *
 * @author xBlackCat
 */
public class SqlStringUtilsTest {
    @Test
   public void simpleCheck() {
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, ConverterInfo.SqlArg> map = Collections.singletonMap(2, new ConverterInfo.SqlArg(null, 0));
            String sql = "SELECT * FROM {2} a WHERE a.id = ?";

            SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + $1 + \" a WHERE a.id = ?\";\n", bldr.toString());
        }
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, ConverterInfo.SqlArg> map = Collections.singletonMap(2,  new ConverterInfo.SqlArg(null, 0));
            String sql = "SELECT * FROM {2} a WHERE a.{1} = ?";

            SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + $1 + \" a WHERE a.{1} = ?\";\n", bldr.toString());
        }
    }

    @Test
    public void testGetArgumentCount() throws Exception {
        Assert.assertEquals(1, SqlStringUtils.getArgumentCount("SELECT * FROM {2} a WHERE a.id = ?"));
        Assert.assertEquals(1, SqlStringUtils.getArgumentCount("SELECT *, 'table ?' FROM {2} a WHERE a.id = ?"));
        Assert.assertEquals(2, SqlStringUtils.getArgumentCount("SELECT * FROM {2} a WHERE a.id = ? AND a = ? AND `quoted 'str\\`ing' `"));
        Assert.assertEquals(1, SqlStringUtils.getArgumentCount("SELECT * FROM table a WHERE a.id = ?"));
    }
}
