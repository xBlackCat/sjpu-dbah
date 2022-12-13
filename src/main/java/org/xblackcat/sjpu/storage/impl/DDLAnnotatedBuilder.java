package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.xblackcat.sjpu.builder.AnAnnotatedMethodBuilder;
import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.ann.DDL;

import java.lang.reflect.Method;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class DDLAnnotatedBuilder extends AnAnnotatedMethodBuilder<DDL> {
    private ClassPool pool;

    DDLAnnotatedBuilder(ClassPool classPool) {
        super(DDL.class);
        pool = classPool;
    }

    @Override
    public void buildMethod(
            CtClass accessHelper,
            Class<?> targetClass,
            Method m
    ) throws NotFoundException, CannotCompileException {
        final String[] ddls = m.getAnnotation(getAnnotationClass()).value();

        final String methodName = m.getName();
        final Class<?> returnType = m.getReturnType();

        final StringBuilder body = new StringBuilder("{\n");

        if (log.isDebugEnabled()) {
            log.debug("Generate DDL method " + m);
        }

        if (!returnType.equals(void.class)) {
            throw new GeneratorException("Invalid return type for updater in method " + methodName + "(): " + returnType.getName());
        }

        if (ArrayUtils.isNotEmpty(m.getParameterTypes())) {
            throw new GeneratorException("DDL method " + methodName + " should be declared without parameters");
        }

        body.append("java.lang.String sql = null;\n");
        body.append("try {\n");
        body.append(AHBuilderUtils.CN_java_sql_Connection);
        body.append(" con = this.factory.getConnection();\n");
        body.append("try {\n");
        body.append(AHBuilderUtils.CN_java_sql_Statement);
        body.append(" st = con.createStatement();\n");
        body.append("try {\n");
        for (String sql : ddls) {
            body.append("sql = \"");
            body.append(StringEscapeUtils.escapeJava(sql));
            body.append(
                    "\";\n" +
                            "st.executeUpdate(sql);\n"
            );
        }

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
        body.append(
                "(\"Can not execute query \"+sql,e);\n" +
                        "}\n" +
                        "}"
        );

        String methodBody = body.toString();
        final String methodName1 = m.getName();
        final Class<?>[] types = m.getParameterTypes();

        if (log.isTraceEnabled()) {
            log.trace("Method void " + methodName1 + "(...)");
            log.trace("Method body: " + methodBody);
        }

        final CtMethod method = CtNewMethod.make(
                m.getModifiers() | Modifier.FINAL,
                CtClass.voidType,
                methodName1,
                BuilderUtils.toCtClasses(pool, types),
                BuilderUtils.toCtClasses(pool, m.getExceptionTypes()),
                methodBody,
                accessHelper
        );

        accessHelper.addMethod(method);

        if (log.isTraceEnabled()) {
            log.trace("Result method: " + method);
        }
    }
}
