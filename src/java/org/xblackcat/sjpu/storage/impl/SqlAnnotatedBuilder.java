package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.RowSetConsumer;
import org.xblackcat.sjpu.storage.ann.Sql;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.SingletonConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.skel.AMethodBuilder;
import org.xblackcat.sjpu.storage.skel.BuilderUtils;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class SqlAnnotatedBuilder extends AMethodBuilder<Sql> {
    static final Map<Class<?>, String> SET_DECLARATIONS;

    static {
        Map<Class<?>, String> map = new HashMap<>();

        // Integer types
        map.put(long.class, "st.setLong(%1$d, %2$s);\n");
        map.put(
                Long.class,
                "java.lang.Long tmp%1$d = %2$s;\nif (tmp%1$d == null) {\nst.setNull(%1$d);\n} else {\nst.setLong(%1$d, tmp%1$d.longValue());\n}\n"
        );
        map.put(int.class, "st.setInt(%1$d, %2$s);\n");
        map.put(
                Integer.class,
                "java.lang.Integer tmp%1$d = %2$s;\nif (tmp%1$d == null) {\nst.setNull(%1$d);\n} else {\nst.setInt(%1$d, tmp%1$d.intValue());\n}\n"
        );
        map.put(short.class, "st.setShort(%1$d, %2$s);\n");
        map.put(
                Short.class,
                "java.lang.Short tmp%1$d = %2$s;\nif (tmp%1$d == null) {\nst.setNull(%1$d);\n} else {\nst.setShort(%1$d, tmp%1$d.shortValue());\n}\n"
        );
        map.put(byte.class, "st.setByte(%1$d, %2$s);\n");
        map.put(
                Byte.class,
                "java.lang.Byte tmp%1$d = %2$s;\nif (tmp%1$d == null) {\nst.setNull(%1$d);\n} else {\nst.setByte(%1$d, tmp%1$d.byteValue());\n}\n"
        );

        // Float types
        map.put(double.class, "st.setDouble(%1$d, %2$s);\n");
        map.put(
                Double.class,
                "java.lang.Double tmp%1$d = %2$s;\nif (tmp%1$d == null) {\nst.setNull(%1$d);\n} else {\nst.setDouble(%1$d, tmp%1$d.doubleValue());\n}\n"
        );
        map.put(float.class, "st.setFloat(%1$d, %2$s);\n");
        map.put(
                Float.class,
                "java.lang.Float tmp%1$d = %2$s;\nif (tmp%1$d == null) {\nst.setNull(%1$d);\n} else {\nst.setFloat(%1$d, tmp%1$d.floatValue());\n}\n"
        );

        // Boolean type
        map.put(boolean.class, "st.setBoolean(%1$d, %2$s);\n");
        map.put(
                Boolean.class,
                "java.lang.Boolean tmp%1$d = %2$s;\nif (tmp%1$d == null) {\nst.setNull(%1$d);\n} else {\nst.setBoolean(%1$d, tmp%1$d.booleanValue());\n}\n"
        );

        // Other types
        map.put(byte[].class, "st.setBytes(%1$d, %2$s);\n");
        map.put(String.class, "st.setString(%1$d, %2$s);\n");
        map.put(BigDecimal.class, "st.setBigDecimal(%1$d, %2$s);\n");

        // Time classes
        map.put(java.sql.Time.class, "st.setTime(%1$d, %2$s);\n");
        map.put(java.sql.Date.class, "st.setDate(%1$d, %2$s);\n");
        map.put(java.sql.Timestamp.class, "st.setTimestamp(%1$d, %2$s);\n");

        synchronized (BuilderUtils.class) {
            SET_DECLARATIONS = Collections.unmodifiableMap(map);
        }
    }

    public SqlAnnotatedBuilder(TypeMapper typeMapper, Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers) {
        super(Sql.class, typeMapper, rowSetConsumers);
    }

    @Override
    public void buildMethod(CtClass accessHelper, Method m) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final String sql = m.getAnnotation(getAnnotationClass()).value();

        final String methodName = m.getName();

        final Class<?> returnType = m.getReturnType();

        ConverterInfo info = ConverterInfo.analyse(typeMapper, rowSetConsumers, m);
        final Class<?> realReturnType = info.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = info.getConverter();

        final QueryType type;
        {
            final Matcher matcher = BuilderUtils.FIRST_WORD_SQL.matcher(sql);
            if (matcher.find()) {
                final String word = matcher.group(1);
                if ("select".equalsIgnoreCase(word)) {
                    type = QueryType.Select;
                } else if ("insert".equalsIgnoreCase(word)) {
                    type = QueryType.Insert;
                } else if ("update".equalsIgnoreCase(word)) {
                    type = QueryType.Update;
                } else {
                    type = QueryType.Other;
                }
            } else {
                type = QueryType.Other;
            }
        }

        if (type == QueryType.Select && converter == null) {
            throw new StorageSetupException("Converter should be specified for SELECT statement in method " + m.toString());
        }

        ClassPool pool = BuilderUtils.getClassPool(typeMapper.getParentPool(), realReturnType, m.getParameterTypes());

        CtClass ctRealReturnType = pool.get(returnType.getName());
        final StringBuilder body = new StringBuilder("{\n");

        body.append("java.lang.String sql = \"");
        body.append(StringEscapeUtils.escapeJava(sql));
        body.append("\";\n");

        final CtClass targetReturnType;

        final boolean noResult = returnType.equals(void.class);
        final boolean generateWrapper = (type == QueryType.Select || type == QueryType.Insert) &&
                (returnType.isPrimitive() && !noResult);

        if (generateWrapper) {
            assert ctRealReturnType instanceof CtPrimitiveType;
            targetReturnType = pool.get(((CtPrimitiveType) ctRealReturnType).getWrapperName());
        } else {
            targetReturnType = ctRealReturnType;
        }

        if (log.isDebugEnabled()) {
            log.debug("Generate " + type + " method " + m);
        }

        final String returnString;
        final boolean returnKeys;
        if (type == QueryType.Select || type == QueryType.Insert) {
            if (info.getConsumeIndex() == null) {
                if (noResult) {
                    if (type == QueryType.Select) {
                        throw new StorageSetupException(
                                "Consumer should be specified in the method parameters for select method with void return type: " + m
                        );
                    }

                    returnString = "";
                    returnKeys = false;
                } else {
                    Class<? extends IRowSetConsumer> rowSetConsumer;
                    final RowSetConsumer annConsumer = m.getAnnotation(RowSetConsumer.class);
                    if (annConsumer != null) {
                        rowSetConsumer = annConsumer.value();
                    } else {
                        rowSetConsumer = hasRowSetConsumer(returnType, realReturnType);
                    }
                    if (rowSetConsumer == null) {
                        rowSetConsumer = SingletonConsumer.class;
                    }

                    body.append(BuilderUtils.CN_IRowSetConsumer);
                    body.append(" consumer = new ");
                    body.append(BuilderUtils.getName(rowSetConsumer));
                    if (hasClassParameter(rowSetConsumer)) {
                        body.append("(");
                        body.append(BuilderUtils.getName(realReturnType));
                        body.append(".class);");
                    } else {
                        body.append("();");
                    }

                    returnString = "return (" + BuilderUtils.getName(targetReturnType) + ") consumer.getRowsHolder();\n";
                    returnKeys = true;
                }
            } else {
                if (!noResult) {
                    if (type == QueryType.Select) {
                        throw new StorageSetupException("Consumer can't be used with non-void return type: " + m.toString());
                    }
                    if (int.class.equals(returnType)) {
                        returnString = "return rows;";
                    } else {
                        throw new StorageSetupException(
                                "Insert method with consumer can have only void or int return type: " +
                                        m.toString()
                        );
                    }
                } else {
                    returnString = "";
                }

                body.append(BuilderUtils.CN_IRowConsumer);
                body.append(" consumer = $args[");
                body.append(info.getConsumeIndex());
                body.append("];");

                returnKeys = true;
            }

            BuilderUtils.checkConverterInstance(pool, converter);

            body.append(BuilderUtils.CN_IToObjectConverter);
            body.append(" converter = ");
            body.append(BuilderUtils.getName(converter));
            body.append(".Instance.I;\n");
        } else {
            if (returnType.equals(int.class)) {
                ctRealReturnType = CtClass.intType;
                returnString = "return rows;";
            } else if (noResult) {
                ctRealReturnType = CtClass.voidType;
                returnString = "";
            } else {
                throw new StorageSetupException(
                        "Invalid return type for updater in method " +
                                methodName +
                                "(...): " +
                                returnType.getName()
                );
            }

            returnKeys = false;
        }

        body.append("try {\n");
        body.append(BuilderUtils.CN_java_sql_Connection);
        body.append(" con = this.factory.getConnection();\n");
        body.append("try {\n");
        body.append(BuilderUtils.CN_java_sql_PreparedStatement);
        body.append(" st = con.prepareStatement(\nsql,\n");
        body.append(BuilderUtils.CN_java_sql_Statement);
        if (type == QueryType.Update && void.class.equals(returnType)) {
            body.append(".RETURN_GENERATED_KEYS");
        } else {
            body.append(".NO_GENERATED_KEYS");
        }
        body.append(");\n");
        body.append("try {\n");

        setParameters(info.getArgumentList(), body);

        final boolean processResultSet;
        switch (type) {
            case Select:
                processResultSet = true;
                body.append(BuilderUtils.CN_java_sql_ResultSet);
                body.append(" rs = st.executeQuery();\n");
                break;
            case Insert:
                processResultSet = returnKeys;
                body.append("int rows = st.executeUpdate();\n");
                body.append(BuilderUtils.CN_java_sql_ResultSet);
                body.append(" rs = st.getGeneratedKeys();\n");
                break;
            case Update:
            case Other:
            default:
                processResultSet = false;
                body.append("int rows = st.executeUpdate();\n");
                break;
        }

        if (processResultSet) {
            body.append(
                    "try {\n" +
                            "while (rs.next()) {\n" +
                            "java.lang.Object obj = converter.convert(rs);\n" +
                            "if (consumer.consume(obj)) {\n" +
                            "break;\n" +
                            "}\n" +
                            "}\n" +
                            "} catch ("
            );
            body.append(BuilderUtils.CN_ConsumeException);
            body.append(
                    " e) {\n" +
                            "throw new "
            );
            body.append(BuilderUtils.CN_StorageException);
            body.append("(\"Can not consume result for query \"+");
            body.append(BuilderUtils.CN_StorageUtils);
            body.append(
                    ".constructDebugSQL(sql, $args),e);\n" +
                            "} catch (java.lang.RuntimeException e) {\n" +
                            "throw new "
            );
            body.append(BuilderUtils.CN_StorageException);
            body.append("(\"Unexpected exception occurs while consuming result for query \"+");
            body.append(BuilderUtils.CN_StorageUtils);
            body.append(
                    ".constructDebugSQL(sql, $args),e);\n" +
                            "} finally {\n" +
                            "rs.close();\n" +
                            "}\n"
            );
        }

        body.append(returnString);
        body.append(
                "} finally {\n" +
                        "st.close();\n" +
                        "}\n" +
                        "} finally {\n" +
                        "con.close();\n" +
                        "}\n" +
                        "} catch ("
        );
        body.append(BuilderUtils.CN_java_sql_SQLException);
        body.append(
                " e) {\n" +
                        "throw new "
        );
        body.append(BuilderUtils.CN_StorageException);
        body.append("(\"Can not execute query \"+");
        body.append(BuilderUtils.CN_StorageUtils);
        body.append(
                ".constructDebugSQL(sql, $args),e);\n" +
                        "}\n" +
                        "}"
        );

        addMethod(accessHelper, m, ctRealReturnType, targetReturnType, body.toString(), pool);
    }

    private boolean hasClassParameter(Class<? extends IRowSetConsumer> consumer) throws StorageSetupException {
        final Constructor<?>[] constructors = consumer.getConstructors();
        boolean hasDefault = false;
        for (Constructor<?> c : constructors) {
            final Class<?>[] types = c.getParameterTypes();
            if (types.length == 1 && types[0].equals(Class.class)) {
                return true;
            } else if (types.length == 0) {
                hasDefault = true;
            }
        }

        if (!hasDefault) {
            throw new StorageSetupException(
                    "Row set consumer should have either default constructor or a constructor with one Class<?> parameter"
            );
        }

        return false;
    }

    protected void setParameters(List<ConverterInfo.Arg> types, StringBuilder body) {
        int idx = 0;

        for (ConverterInfo.Arg arg : types) {
            idx++;
            Class<?> type = arg.clazz;
            final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(type);

            final String initString;
            if (typeMap != null) {
                final String value = "(" + BuilderUtils.getName(typeMap.getDbType()) + ") " +
                        typeMapper.getTypeMapInstanceRef(type) + ".forStore($" + (arg.idx + 1) + ")";
                initString = setParamValue(idx, typeMap.getDbType(), value);
            } else {
                initString = setParamValue(idx, type, "$" + (arg.idx + 1));
            }
            body.append(initString);
        }
    }

    protected String setParamValue(int idx, Class<?> type, String value) {
        final String setLine = SET_DECLARATIONS.get(type);
        if (setLine == null) {
            throw new StorageSetupException("Can't process type " + type.getName());
        }

        return String.format(setLine, idx, value);
    }

    protected void addMethod(
            CtClass accessHelper,
            Method m,
            CtClass realReturnType,
            CtClass targetReturnType,
            String methodBody,
            ClassPool pool
    ) throws CannotCompileException, NotFoundException {
        final boolean generateWrapper = !targetReturnType.equals(realReturnType);

        final String methodName = m.getName();
        final Class<?>[] types = m.getParameterTypes();

        final String targetMethodName;
        final int targetModifiers;

        if (log.isTraceEnabled()) {
            log.trace("Method " + realReturnType.getName() + " " + methodName + "(...)");
            if (generateWrapper) {
                log.trace(" [ + Wrapper for unboxing ]");
            }
            log.trace("Method body: " + methodBody);
        }

        if (generateWrapper) {
            targetMethodName = "$" + methodName + "$Wrap";
            targetModifiers = Modifier.PRIVATE;
        } else {
            targetMethodName = methodName;
            targetModifiers = m.getModifiers() | Modifier.FINAL;
        }


        final CtMethod method = CtNewMethod.make(
                targetModifiers,
                targetReturnType,
                targetMethodName,
                BuilderUtils.toCtClasses(pool, types),
                BuilderUtils.toCtClasses(pool, m.getExceptionTypes()),
                methodBody,
                accessHelper
        );

        accessHelper.addMethod(method);

        if (generateWrapper) {
            final String unwrapBody = "{\n" +
                    targetReturnType.getName() +
                    " value = " +
                    targetMethodName +
                    "($$);\n" +
                    "if (value == null) {\nthrow new java.lang.NullPointerException(\"Can't unwrap null value.\");\n}\n" +
                    "return value." +
                    BuilderUtils.getUnwrapMethodName(realReturnType) +
                    "();\n}";

            final CtMethod coverMethod = CtNewMethod.make(
                    m.getModifiers() | Modifier.FINAL,
                    realReturnType,
                    methodName,
                    BuilderUtils.toCtClasses(pool, types),
                    BuilderUtils.toCtClasses(pool, m.getExceptionTypes()),
                    unwrapBody,
                    accessHelper
            );

            accessHelper.addMethod(coverMethod);
        }

        if (log.isTraceEnabled()) {
            log.trace("Result method: " + method);
        }
    }
}
