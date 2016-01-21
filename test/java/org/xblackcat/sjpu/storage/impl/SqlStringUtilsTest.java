package org.xblackcat.sjpu.storage.impl;

import org.junit.Assert;
import org.junit.Test;
import org.xblackcat.sjpu.storage.converter.builder.Arg;
import org.xblackcat.sjpu.storage.converter.builder.ArgIdx;
import org.xblackcat.sjpu.storage.converter.builder.ArgInfo;

import java.net.URL;
import java.util.*;

/**
 * 14.06.2014 23:58
 *
 * @author xBlackCat
 */
public class SqlStringUtilsTest {
    @Test
    public void simpleCheck() {
        final Arg staticArg1 = new Arg(long.class, 1);
        final Arg sqlPartArg0 = new Arg(String.class, null, new ArgIdx(0, false));
        final Arg sqlOptArgJoinByte0 = new Arg(Byte.class, " JOIN table ON (table.id = ?)", new ArgIdx(0, true));
        final Arg sqlPartOptArg0 = new Arg(Byte.class, "?", new ArgIdx(0, false));

        {
            Collection<Arg> staticArgs = Collections.singletonList(staticArg1);
            StringBuilder bldr = new StringBuilder();
            Map<Integer, Arg> map = Collections.singletonMap(2, sqlPartArg0);
            String sql = "SELECT * FROM {2} a WHERE a.id = ?";

            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, staticArgs, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + $1 + \" a WHERE a.id = ?\";\n", bldr.toString());
            Assert.assertEquals(staticArgs, argIdxes);
        }
        {
            Collection<Arg> staticArgs = Collections.singletonList(staticArg1);
            StringBuilder bldr = new StringBuilder();
            Map<Integer, Arg> map = Collections.singletonMap(2, sqlPartArg0);
            String sql = "SELECT * FROM {2} a WHERE a.{1} = ?";

            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, staticArgs, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + $1 + \" a WHERE a.{1} = ?\";\n", bldr.toString());
            Assert.assertEquals(staticArgs, argIdxes);
        }
        {
            Collection<Arg> staticArgs = Collections.singletonList(staticArg1);
            StringBuilder bldr = new StringBuilder();
            Map<Integer, Arg> map = Collections.singletonMap(2, sqlOptArgJoinByte0);
            String sql = "SELECT * FROM {2} a WHERE a.{1} = ?";

            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, staticArgs, map);

            final String expected = "java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n" +
                    "sqlBuilder.append(\"SELECT * FROM \");\n" +
                    "if ($1 != null) {\n" +
                    "sqlBuilder.append(\" JOIN table ON (table.id = ?)\");\n" +
                    "}\n" +
                    "sqlBuilder.append(\" a WHERE a.{1} = ?\");\n" +
                    "java.lang.String sql = sqlBuilder.toString();\n";
            Assert.assertEquals(expected, bldr.toString());
            Assert.assertEquals(Arrays.asList(sqlOptArgJoinByte0, staticArg1), argIdxes);
        }
        {
            Collection<Arg> staticArgs = Collections.singleton(staticArg1);
            StringBuilder bldr = new StringBuilder();
            Map<Integer, Arg> map = Collections.singletonMap(2, sqlPartOptArg0);
            String sql = "SELECT * FROM {2} a WHERE a.{1} = ?";

            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, staticArgs, map);

            Assert.assertEquals("java.lang.String sql = \"SELECT * FROM \" + \"?\" + \" a WHERE a.{1} = ?\";\n", bldr.toString());
            Assert.assertEquals(Arrays.asList(sqlPartOptArg0, staticArg1), argIdxes);

        }
        {
            StringBuilder bldr = new StringBuilder();
            Map<Integer, Arg> map = Collections.singletonMap(2, sqlPartOptArg0);
            String sql = "SELECT * FROM table a WHERE a.id = {2} and a.other_id = {2}";

            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, Collections.emptyList(), map);

            Assert.assertEquals(
                    "java.lang.String sql = \"SELECT * FROM table a WHERE a.id = \" + \"?\" + \" and a.other_id = \" + \"?\" + \"\";\n",
                    bldr.toString()
            );
            Assert.assertEquals(Arrays.asList(sqlPartOptArg0, sqlPartOptArg0), argIdxes);
        }
        {
            Collection<Arg> staticArgs = Collections.singleton(staticArg1);
            StringBuilder bldr = new StringBuilder();
            Map<Integer, Arg> map = new HashMap<>();
            final Arg sqlArg2 = new Arg(Byte.class, "?", new ArgIdx(2, false));
            map.put(2, sqlArg2); // Emulation of @SqlArg
            map.put(1, sqlOptArgJoinByte0);
            String sql = "SELECT * FROM table a {1} WHERE a.id = {2} and a.id = ? and a.other_id = {2}";

            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, staticArgs, map);

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
            Assert.assertEquals(Arrays.asList(sqlOptArgJoinByte0, sqlArg2, staticArg1, sqlArg2), argIdxes);
        }
        {
            final Arg staticArg3 = new Arg(URL.class, 3);
            Collection<Arg> staticArgs = Arrays.asList(staticArg1, staticArg3);
            final Arg sqlArg2 = new Arg(Byte.class, "?", new ArgIdx(2, false));
            final Arg sqlVarArg0 = new Arg(List.class, "?", new ArgInfo(Integer.class, ","), new ArgIdx(0));

            Map<Integer, Arg> map = new HashMap<>();
            map.put(2, sqlArg2); // Emulation of @SqlArg
            map.put(1, sqlVarArg0); // Emulation of @SqlPart+@SqlVarArg
            String sql = "SELECT * FROM table a WHERE a.name IN ({1}) AND a.id = {2} AND a.id = ? AND a.url = ? AND a.other_id = {2}";

            StringBuilder bldr = new StringBuilder();
            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, staticArgs, map);

            final String expected = "java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n" +
                    "sqlBuilder.append(\"SELECT * FROM table a WHERE a.name IN (\");\n" +
                    "if ($1 != null) {\n" +
                    "boolean firstElement = true;\n" +
                    "java.util.Iterator _it = $1.iterator();\n" +
                    "while (_it.hasNext()) {\n" +
                    "_it.next();\n" +
                    "if (firstElement) {\n" +
                    "firstElement = false;\n" +
                    "} else {\n" +
                    "sqlBuilder.append(\",\");\n" +
                    "}\n" +
                    "sqlBuilder.append(\"?\");\n" +
                    "}\n" +
                    "if (firstElement) {\n" +
                    "throw new java.lang.IllegalArgumentException(\"Empty collection for vararg argument #0\");\n" +
                    "}\n" +
                    "} else {\n" +
                    "throw new java.lang.IllegalArgumentException(\"Null collection for vararg argument #0\");\n" +
                    "}\n" +
                    "sqlBuilder.append(\") AND a.id = \");\n" +
                    "sqlBuilder.append(\"?\");\n" +
                    "sqlBuilder.append(\" AND a.id = ? AND a.url = ? AND a.other_id = \");\n" +
                    "sqlBuilder.append(\"?\");\n" +
                    "sqlBuilder.append(\"\");\n" +
                    "java.lang.String sql = sqlBuilder.toString();\n";

            Assert.assertEquals(expected, bldr.toString());
            Assert.assertEquals(Arrays.asList(sqlVarArg0, sqlArg2, staticArg1, staticArg3, sqlArg2), argIdxes);
        }
        {
            final Arg sqlVarArg0 = new Arg(
                    List.class,
                    "(?,?,?)",
                    new ArgInfo(URL.class, ","),
                    new ArgIdx(0),
                    new ArgInfo(int.class, "getPort"),
                    new ArgInfo(String.class, "getHost"),
                    new ArgInfo(String.class, "getQuery")
            );

            Map<Integer, Arg> map = new HashMap<>();
            map.put(0, sqlVarArg0); // Emulation of @SqlPart+@SqlVarArg
            String sql = "INSERT INTO table (col1, col2, col3) VALUES {0}";

            StringBuilder bldr = new StringBuilder();
            final Collection<Arg> argIdxes = SqlStringUtils.appendSqlWithParts(bldr, sql, Collections.emptyList(), map);

            final String expected = "java.lang.StringBuilder sqlBuilder = new java.lang.StringBuilder();\n" +
                    "sqlBuilder.append(\"INSERT INTO table (col1, col2, col3) VALUES \");\n" +
                    "if ($1 != null) {\n" +
                    "boolean firstElement = true;\n" +
                    "java.util.Iterator _it = $1.iterator();\n" +
                    "while (_it.hasNext()) {\n" +
                    "_it.next();\n" +
                    "if (firstElement) {\n" +
                    "firstElement = false;\n" +
                    "} else {\n" +
                    "sqlBuilder.append(\",\");\n" +
                    "}\n" +
                    "sqlBuilder.append(\"(?,?,?)\");\n" +
                    "}\n" +
                    "if (firstElement) {\n" +
                    "throw new java.lang.IllegalArgumentException(\"Empty collection for vararg argument #0\");\n" +
                    "}\n" +
                    "} else {\n" +
                    "throw new java.lang.IllegalArgumentException(\"Null collection for vararg argument #0\");\n" +
                    "}\n" +
                    "sqlBuilder.append(\"\");\n" +
                    "java.lang.String sql = sqlBuilder.toString();\n";

            Assert.assertEquals(expected, bldr.toString());
            Assert.assertEquals(Collections.singletonList(sqlVarArg0), argIdxes);
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
