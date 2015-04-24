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
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class FunctionalAHBuilder extends ASelectAnnotatedBuilder<SqlType> {
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
}
