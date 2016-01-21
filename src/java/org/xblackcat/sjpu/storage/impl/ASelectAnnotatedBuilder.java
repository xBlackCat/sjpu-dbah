package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.StorageUtils;
import org.xblackcat.sjpu.storage.ann.QueryType;
import org.xblackcat.sjpu.storage.ann.RowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.consumer.SingletonConsumer;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.converter.builder.Arg;
import org.xblackcat.sjpu.storage.converter.builder.ArgIdx;
import org.xblackcat.sjpu.storage.converter.builder.ArgInfo;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * 24.04.2015 18:03
 *
 * @author xBlackCat
 */
public abstract class ASelectAnnotatedBuilder<A extends Annotation> extends AMappableMethodBuilder<A> {
    public ASelectAnnotatedBuilder(
            Class<A> annClass,
            TypeMapper typeMapper,
            Map<Class<?>, Class<? extends IRowSetConsumer>> rowSetConsumers
    ) {
        super(annClass, typeMapper, rowSetConsumers);
    }

    protected boolean hasClassParameter(Class<? extends IRowSetConsumer> consumer) throws GeneratorException {
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

    protected void setParameters(
            ClassPool pool,
            Collection<Arg> types,
            StringBuilder body
    ) throws NotFoundException, ClassNotFoundException {
        body.append("int idx = 0;\n");

        for (Arg arg : types) {
            final Class<?> type = arg.typeRawClass;

            final ArgIdx argIdx = arg.idx;
            final int idx = argIdx.idx + 1;

            final ArgInfo varArgInfo = arg.varArgInfo;
            if (argIdx.optional) {
                body.append("if ($");
                body.append(idx);
                body.append(" != null) ");
            }
            body.append("{\n");

            final boolean noExpanding = arg.expandedArgs == null || arg.expandedArgs.length == 0;
            final boolean isVarArg = varArgInfo != null;
            final boolean isArray = isVarArg && type.isArray();

            final String valueRef;

            if (isVarArg) {
                final String elementClassName = BuilderUtils.getName(varArgInfo.clazz);
                if (isArray) {
                    body.append("for (int _i = 0; _i < $");
                    body.append(idx);
                    body.append(".length; _i++ ) {\n");
                    body.append(elementClassName);
                    body.append(" _el = $");
                    body.append(idx);
                    body.append("[_i];\n");
                } else {
                    body.append("java.util.Iterator _it = $");
                    body.append(idx);
                    body.append(".iterator();\nwhile (_it.hasNext()) {\n");
                    body.append(elementClassName);
                    body.append(" _el = (");
                    body.append(elementClassName);
                    body.append(") _it.next();\n");
                }
                valueRef = "_el";
            } else {
                valueRef = "$" + idx;
            }

            if (noExpanding) {
                if (isVarArg) {
                    final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(varArgInfo.clazz);
                    appendSetArg(body, typeMap, valueRef, varArgInfo.clazz);
                } else {
                    final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(type);
                    appendSetArg(body, typeMap, valueRef, type);
                }
            } else {
                for (ArgInfo argInfo : arg.expandedArgs) {
                    final String subArgRef = "_" + idx + "_" + argInfo.methodName;
                    final Class<?> argTypeExpect;

                    final Class<?> clazz = argInfo.clazz;
                    final boolean isPrimitive = clazz.isPrimitive();
                    if (isPrimitive) {
                        argTypeExpect = Class.forName(((CtPrimitiveType) pool.get(clazz.getName())).getWrapperName());
                    } else {
                        argTypeExpect = clazz;
                    }

                    final String argClassFQN = BuilderUtils.getName(argTypeExpect);
                    body.append(argClassFQN);
                    body.append(" ");
                    body.append(subArgRef);
                    body.append(" = ((");
                    body.append(argClassFQN);
                    body.append(") (");
                    body.append(valueRef);
                    body.append(" == null ? null : ");
                    if (isPrimitive) {
                        body.append(argClassFQN);
                        body.append(".valueOf(");
                    }
                    body.append(valueRef);
                    body.append(".");
                    body.append(argInfo.methodName);
                    body.append("()");
                    if (isPrimitive) {
                        body.append(")");
                    }
                    body.append("));\n");
                    final ITypeMap<?, ?> subTypeMap = typeMapper.hasTypeMap(argTypeExpect);

                    appendSetArg(body, subTypeMap, subArgRef, argTypeExpect);
                }
            }
            if (isVarArg) {
                body.append("}\n");
            }
            body.append("}\n");

        }
    }

    private void appendSetArg(StringBuilder body, ITypeMap<?, ?> typeMap, String argRef, Class<?> argTypeExpect) {
        final Class<?> argType;
        final String value;
        if (typeMap != null) {
            final String typeMapInstanceRef = typeMapper.getTypeMapInstanceRef(argTypeExpect);

            argType = typeMap.getDbType();
            value = "(" + BuilderUtils.getName(argType) + ") " + typeMapInstanceRef + ".forStore(con, " + argRef + ")";
        } else {
            argType = argTypeExpect;
            value = argRef;
        }

        body.append("idx++;\n");
        body.append(AHBuilderUtils.setParamValue(argType, value));
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

    @Override
    public void buildMethod(
            CtClass accessHelper,
            Class<?> targetClass,
            Method m
    ) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final String methodName = m.getName();

        final Class<?> returnType = m.getReturnType();

        ConverterInfo info = ConverterInfo.analyse(typeMapper, rowSetConsumers, m);
        final Class<?> realReturnType = info.getRealReturnType();
        Class<? extends IToObjectConverter<?>> converter = info.getConverter();

        final QueryType type = getQueryType(m);

        if (type == QueryType.Select && converter == null) {
            throw new GeneratorException("Converter should be specified for SELECT statement in method " + m.toString());
        }

        ClassPool pool = BuilderUtils.getClassPool(typeMapper.getParentPool(), realReturnType, m.getParameterTypes());

        CtClass ctRealReturnType = pool.get(returnType.getName());
        final StringBuilder body = new StringBuilder("{\n");

        final Collection<Arg> args = appendDefineSql(body, info, m);

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
            returnKeys = type != QueryType.Select;
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
                    returnKeys = type != QueryType.Select;
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

                returnKeys = type != QueryType.Select;
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

        appendPrepareStatement(body, m, returnKeys);
        body.append("try {\n");

        setParameters(pool, args, body);

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

        String debugSqlBuilder = AHBuilderUtils.CN_StorageUtils +
                ".constructDebugSQL(sql, " +
                StorageUtils.converterArgsToJava(args) +
                ", $args)";

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
            body.append("(\"Failed to consume result for query \"+");
            body.append(debugSqlBuilder);
            body.append(",e);\n");
            body.append("} catch (java.lang.RuntimeException e) {\n");
            body.append("throw new ");
            body.append(AHBuilderUtils.CN_StorageException);
            body.append("(\"Unexpected exception occurs while consuming result for query \"+");
            body.append(debugSqlBuilder);
            body.append(",e);\n");
            body.append("} finally {\n" +
                                "rs.close();\n" +
                                "}\n"
            );
        }

        body.append(returnString);
        appendCloseStatement(body);
        body.append("} catch (");
        body.append(AHBuilderUtils.CN_java_sql_SQLException);
        body.append(
                " e) {\n" +
                        "throw new "
        );
        body.append(AHBuilderUtils.CN_StorageException);
        body.append("(\"Can not execute query \"+");
        body.append(debugSqlBuilder);
        body.append(",e);\n");
        body.append("}\n" +
                            "}"
        );

        addMethod(accessHelper, m, ctRealReturnType, targetReturnType, body.toString(), pool);
    }

    protected void appendCloseStatement(StringBuilder body) {
        body.append(
                "} finally {\n" +
                        "st.close();\n" +
                        "}\n" +
                        "} finally {\n" +
                        "con.close();\n" +
                        "}\n"
        );
    }

    protected void appendPrepareStatement(StringBuilder body, Method m, boolean returnKeys) {
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
    }

    protected abstract QueryType getQueryType(Method m);

    protected abstract Collection<Arg> appendDefineSql(StringBuilder body, ConverterInfo info, Method m);
}
