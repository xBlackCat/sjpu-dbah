package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.xblackcat.sjpu.skel.BuilderUtils;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.RowSetConsumer;
import org.xblackcat.sjpu.storage.ann.SqlPart;
import org.xblackcat.sjpu.storage.ann.SqlType;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.SingletonConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class FunctionalAHBuilder extends AMappableMethodBuilder<SqlType> {
    static final Map<Class<?>, String> SET_DECLARATIONS;

    static {
        Map<Class<?>, String> map = new HashMap<>();

        // Integer types
        map.put(long.class, "st.setLong(idx, %s);\n");
        map.put(
                Long.class,
                "{\njava.lang.Long tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setLong(idx, tmpVal.longValue());\n}\n}\n"
        );
        map.put(int.class, "st.setInt(idx, %s);\n");
        map.put(
                Integer.class,
                "{\njava.lang.Integer tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setInt(idx, tmpVal.intValue());\n}\n}\n"
        );
        map.put(short.class, "st.setShort(idx, %s);\n");
        map.put(
                Short.class,
                "{\njava.lang.Short tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setShort(idx, tmpVal.shortValue());\n}\n}\n"
        );
        map.put(byte.class, "st.setByte(idx, %s);\n");
        map.put(
                Byte.class,
                "{\njava.lang.Byte tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setByte(idx, tmpVal.byteValue());\n}\n}\n"
        );

        // Float types
        map.put(double.class, "st.setDouble(idx, %s);\n");
        map.put(
                Double.class,
                "{\njava.lang.Double tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setDouble(idx, tmpVal.doubleValue());\n}\n}\n"
        );
        map.put(float.class, "st.setFloat(idx, %s);\n");
        map.put(
                Float.class,
                "{\njava.lang.Float tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setFloat(idx, tmpVal.floatValue());\n}\n}\n"
        );

        // Boolean type
        map.put(boolean.class, "st.setBoolean(idx, %s);\n");
        map.put(
                Boolean.class,
                "{\njava.lang.Boolean tmpVal = %s;\nif (tmpVal == null) {\nst.setNull(idx, 0);\n} else {\nst.setBoolean(idx, tmpVal.booleanValue());\n}\n}\n"
        );

        // Other types
        map.put(byte[].class, "st.setBytes(idx, %s);\n");
        map.put(String.class, "st.setString(idx, %s);\n");
        map.put(BigDecimal.class, "st.setBigDecimal(idx, %s);\n");

        // Time classes
        map.put(java.sql.Time.class, "st.setTime(idx, %s);\n");
        map.put(java.sql.Date.class, "st.setDate(idx, %s);\n");
        map.put(java.sql.Timestamp.class, "st.setTimestamp(idx, %s);\n");

        synchronized (AHBuilderUtils.class) {
            SET_DECLARATIONS = Collections.unmodifiableMap(map);
        }
    }

    public FunctionalAHBuilder(TypeMapper typeMapper, Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers) {
        super(SqlType.class, typeMapper, rowSetConsumers);
    }

    @Override
    public void buildMethod(CtClass accessHelper, Method m) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final QueryType type = m.getAnnotation(getAnnotationClass()).value();

        final String methodName = m.getName();

        final Class<?> returnType = m.getReturnType();

        ConverterInfo info = ConverterInfo.analyse(typeMapper, rowSetConsumers, m);
        if (!info.getSqlParts().isEmpty()) {
            throw new GeneratorException(BuilderUtils.getName(SqlPart.class) + " annotation is not allowed in functional AH");
        }
        final Class<?> realReturnType = info.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = info.getConverter();

        if (type == QueryType.Select && converter == null) {
            throw new GeneratorException("Converter should be specified for SELECT statement in method " + m.toString());
        }

        ClassPool pool = BuilderUtils.getClassPool(typeMapper.getParentPool(), realReturnType, m.getParameterTypes());

        CtClass ctRealReturnType = pool.get(returnType.getName());
        final StringBuilder body = new StringBuilder("{\n");

        body.append("java.lang.String sql = getSql();\n");

        final CtClass targetReturnType;

        final boolean noResult = returnType.equals(void.class);
        final boolean returnRowsAmount = returnType.equals(int.class);
        final boolean generateWrapper;
        switch (type) {
            case Select:
                generateWrapper = returnType.isPrimitive() && !noResult;
                break;
            case Insert:
                // For INSERT statements return type int means affected rows
                generateWrapper = returnType.isPrimitive() && !noResult && !returnRowsAmount;
                break;
            default:
                generateWrapper = false;
                break;
        }

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
        if (info.getRawProcessorParamIndex() != null) {
            if (!noResult) {
                throw new GeneratorException("No return value is allowed for method with raw consumer");
            }

            returnString = "";
            returnKeys = true;
        } else if (type == QueryType.Select || type == QueryType.Insert) {
            if (info.getConsumeIndex() == null) {
                if (noResult) {
                    if (type == QueryType.Select) {
                        throw new GeneratorException(
                                "Consumer should be specified in the method parameters for select method with void return type: " + m
                        );
                    }

                    returnString = "";
                    returnKeys = false;
                } else if (type == QueryType.Insert && returnRowsAmount) {
                    returnString = "return rows;\n";
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

                    body.append(AHBuilderUtils.CN_IRowSetConsumer);
                    body.append(" consumer = new ");
                    body.append(BuilderUtils.getName(rowSetConsumer));
                    if (hasClassParameter(rowSetConsumer)) {
                        body.append("(");
                        body.append(BuilderUtils.getName(realReturnType));
                        body.append(".class);\n");
                    } else {
                        body.append("();\n");
                    }

                    returnString = "return (" + BuilderUtils.getName(targetReturnType) + ") consumer.getRowsHolder();\n";
                    returnKeys = true;
                }
            } else {
                if (!noResult) {
                    if (type == QueryType.Select) {
                        throw new GeneratorException("Consumer can't be used with non-void return type: " + m.toString());
                    }
                    if (int.class.equals(returnType)) {
                        returnString = "return rows;";
                    } else {
                        throw new GeneratorException(
                                "Insert method with consumer can have only void or int return type: " + m.toString()
                        );
                    }
                } else {
                    returnString = "";
                }

                body.append(AHBuilderUtils.CN_IRowConsumer);
                body.append(" consumer = $");
                body.append(info.getConsumeIndex() + 1);
                body.append(";\n");

                returnKeys = true;
            }

            AHBuilderUtils.checkConverterInstance(pool, converter);

            body.append(AHBuilderUtils.CN_IToObjectConverter);
            body.append(" converter = ");
            body.append(BuilderUtils.getName(converter));
            body.append(".Instance.I;\n");
        } else {
            if (returnRowsAmount) {
                ctRealReturnType = CtClass.intType;
                returnString = "return rows;";
            } else if (noResult) {
                ctRealReturnType = CtClass.voidType;
                returnString = "";
            } else {
                throw new GeneratorException(
                        "Invalid return type for updater in method " + methodName + "(...): " + returnType.getName()
                );
            }

            returnKeys = false;
        }

        body.append("try {\n");
        body.append(AHBuilderUtils.CN_java_sql_Connection);
        body.append(" con = this.factory.getConnection();\n");
        body.append("try {\n");
        body.append(AHBuilderUtils.CN_java_sql_PreparedStatement);
        body.append(" st = con.prepareStatement(\nsql,\n");
        body.append(AHBuilderUtils.CN_java_sql_Statement);
        if (returnKeys) {
            body.append(".RETURN_GENERATED_KEYS");
        } else {
            body.append(".NO_GENERATED_KEYS");
        }
        body.append(");\n");
        body.append("try {\n");


        final Class<?>[] types = m.getParameterTypes();
        final Collection<ConverterInfo.Arg> args = substituteOptionalArgs(info.getArgumentList(), Collections.emptyList(), types);
        setParameters(args, body);

        final boolean processResultSet;
        switch (type) {
            case Select:
                processResultSet = true;
                body.append(AHBuilderUtils.CN_java_sql_ResultSet);
                body.append(" rs = st.executeQuery();\n");
                break;
            case Insert:
                processResultSet = returnKeys;
                body.append("int rows = st.executeUpdate();\n");
                if (returnKeys) {
                    body.append(AHBuilderUtils.CN_java_sql_ResultSet);
                    body.append(" rs = st.getGeneratedKeys();\n");
                }
                break;
            case Update:
            case Other:
            default:
                processResultSet = false;
                body.append("int rows = st.executeUpdate();\n");
                break;
        }

        if (processResultSet) {
            body.append("try {\n");
            if (info.getRawProcessorParamIndex() == null) {
                body.append(
                        "while (rs.next()) {\n" +
                                "java.lang.Object obj = converter.convert(rs);\n" +
                                "if (consumer.consume(obj)) {\n" +
                                "break;\n" +
                                "}\n" +
                                "}\n" +
                                "} catch ("
                );
                body.append(AHBuilderUtils.CN_ConsumeException);
            } else {
                body.append("((");
                body.append(AHBuilderUtils.CN_IRawProcessor);
                body.append(")$");
                body.append(info.getRawProcessorParamIndex() + 1);
                body.append(").process(rs);\n} catch (");
                body.append(AHBuilderUtils.CN_java_sql_SQLException);
            }
            body.append(
                    " e) {\n" +
                            "throw new "
            );
            body.append(AHBuilderUtils.CN_StorageException);
            body.append("(\"Can not consume result for query \"+");
            body.append(AHBuilderUtils.CN_StorageUtils);
            body.append(
                    ".constructDebugSQL(sql, $args),e);\n" +
                            "} catch (java.lang.RuntimeException e) {\n" +
                            "throw new "
            );
            body.append(AHBuilderUtils.CN_StorageException);
            body.append("(\"Unexpected exception occurs while consuming result for query \"+");
            body.append(AHBuilderUtils.CN_StorageUtils);
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
        body.append(AHBuilderUtils.CN_java_sql_SQLException);
        body.append(
                " e) {\n" +
                        "throw new "
        );
        body.append(AHBuilderUtils.CN_StorageException);
        body.append("(\"Can not execute query \"+");
        body.append(AHBuilderUtils.CN_StorageUtils);
        body.append(
                ".constructDebugSQL(sql, $args),e);\n" +
                        "}\n" +
                        "}"
        );

        addMethod(accessHelper, m, ctRealReturnType, targetReturnType, body.toString(), pool);
    }

    protected static Collection<ConverterInfo.Arg> substituteOptionalArgs(
            Collection<ConverterInfo.Arg> argumentList, List<Integer> optionalIndexes,
            Class<?>... types
    ) {
        final Collection<ConverterInfo.Arg> args;
        if (optionalIndexes == null || optionalIndexes.isEmpty()) {
            args = argumentList;
        } else {
            final Iterator<ConverterInfo.Arg> staticArgs = argumentList.iterator();
            args = new ArrayList<>();
            for (Integer opt : optionalIndexes) {
                if (opt == null) {
                    args.add(staticArgs.next());
                } else {
                    args.add(new ConverterInfo.Arg(types[opt], opt, true));
                }
            }
            while (staticArgs.hasNext()) {
                args.add(staticArgs.next());
            }
        }
        return args;
    }

    private boolean hasClassParameter(Class<? extends IRowSetConsumer> consumer) throws GeneratorException {
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
            throw new GeneratorException(
                    "Row set consumer should have either default constructor or a constructor with one Class<?> parameter"
            );
        }

        return false;
    }

    protected void setParameters(Collection<ConverterInfo.Arg> types, StringBuilder body) {
        body.append("int idx = 0;\n");

        for (ConverterInfo.Arg arg : types) {
            Class<?> type = arg.clazz;
            final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(type);

            final String initString;
            final int idx = arg.idx + 1;
            if (typeMap != null) {
                final String value = "(" + BuilderUtils.getName(typeMap.getDbType()) + ") " +
                        typeMapper.getTypeMapInstanceRef(type) + ".forStore($" + idx + ")";
                initString = setParamValue(typeMap.getDbType(), value);
            } else {
                initString = setParamValue(type, "$" + idx);
            }

            if (arg.optional) {
                body.append("if ($");
                body.append(idx);
                body.append(" != null) {\n");
            }
            body.append("idx++;\n");
            body.append(initString);
            if (arg.optional) {
                body.append("}\n");
            }
        }
    }

    protected String setParamValue(Class<?> type, String value) {
        final String setLine = SET_DECLARATIONS.get(type);
        if (setLine == null) {
            throw new GeneratorException("Can't process type " + type.getName());
        }

        return String.format(setLine, value);
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
