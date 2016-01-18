package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.converter.builder.Arg;
import org.xblackcat.sjpu.storage.converter.builder.SqlArgInfo;

import java.util.*;

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
            Map<Integer, SqlArgInfo> map = Collections.singletonMap(2, new SqlArgInfo(null, 0, true));
            String sql = "SELECT * FROM {2} a WHERE a.id = ?";

            final List<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + $1 + \" a WHERE a.id = ?\";\n", bldr.toString());
            Assert.assertEquals(Collections.emptyList(), argIdxes);
        }
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, SqlArgInfo> map = Collections.singletonMap(2, new SqlArgInfo(null, 0, true));
            String sql = "SELECT * FROM {2} a WHERE a.{1} = ?";

            final List<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + $1 + \" a WHERE a.{1} = ?\";\n", bldr.toString());
            Assert.assertEquals(Collections.emptyList(), argIdxes);
        }
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, SqlArgInfo> map = Collections.singletonMap(2, new SqlArgInfo(" JOIN table ON (table.id = ?)", 0, true));
            String sql = "SELECT * FROM {2} a WHERE a.{1} = ?";

            final List<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            final String expected = "java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n" +
                    "sqlBuilder.append(\"SELECT * FROM \");\n" +
                    "if ($1 != null) {\n" +
                    "sqlBuilder.append(\" JOIN table ON (table.id = ?)\");\n" +
                    "}\n" +
                    "sqlBuilder.append(\" a WHERE a.{1} = ?\");\n" +
                    "java.lang.String sql = sqlBuilder.toString();\n";
            Assert.assertEquals(expected, bldr.toString());
            Assert.assertEquals(Collections.singletonList(new Arg(null, 0, true)), argIdxes);
        }
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, SqlArgInfo> map = Collections.singletonMap(2, new SqlArgInfo("?", 0, false));
            String sql = "SELECT * FROM {2} a WHERE a.{1} = ?";

            final List<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + \"?\" + \" a WHERE a.{1} = ?\";\n", bldr.toString());
            Assert.assertEquals(Collections.singletonList(new Arg(null, 0, false)), argIdxes);

        }
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, SqlArgInfo> map = Collections.singletonMap(2, new SqlArgInfo("?", 0, false));
            String sql = "SELECT * FROM table a WHERE a.id = {2} and a.other_id = {2}";

            final List<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            Assert.assertEquals(
                    "java.lang.String sql = \"SELECT * FROM table a WHERE a.id = \" + \"?\" + \" and a.other_id = \" + \"?\" + \"\";\n",
                    bldr.toString()
            );
            Assert.assertEquals(Arrays.asList(new Arg(null, 0, false), new Arg(null, 0, false)), argIdxes);

        }
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, SqlArgInfo> map = new HashMap<>();
            map.put(2, new SqlArgInfo("?", 1, false)); // Emulation of @SqlArg
            map.put(1, new SqlArgInfo(" JOIN table ON (table.id = ?)", 0, true));
            String sql = "SELECT * FROM table a {1} WHERE a.id = {2} and a.id = ? and a.other_id = {2}";

            final List<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, map);

            final String expected = "java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n" +
                    "sqlBuilder.append(\"SELECT * FROM table a \");\n" +
                    "if ($1 != null) {\n" +
                    "sqlBuilder.append(\" JOIN table ON (table.id = ?)\");\n" +
                    "}\n" +
                    "sqlBuilder.append(\" WHERE a.id = \");\n" +
                    "sqlBuilder.append(\"?\");\n" +
                    "sqlBuilder.append(\" and a.id = ? and a.other_id = \");\n" +
                    "sqlBuilder.append(\"?\");\n" +
                    "sqlBuilder.append(\"\");\n" +
                    "java.lang.String sql = sqlBuilder.toString();\n";

            Assert.assertEquals(expected, bldr.toString());
            Assert.assertEquals(Arrays.asList(new Arg(null, 0, true), new Arg(null, 1, false), null, new Arg(null, 1, false)), argIdxes);

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
