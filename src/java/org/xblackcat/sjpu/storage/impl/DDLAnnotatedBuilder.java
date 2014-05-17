package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageException;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.DDL;
import org.xblackcat.sjpu.storage.skel.BuilderUtils;
import org.xblackcat.sjpu.storage.skel.IMethodBuilder;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class DDLAnnotatedBuilder implements IMethodBuilder<DDL> {
    private static final Log log = LogFactory.getLog(DDLAnnotatedBuilder.class);
    private ClassPool pool;

    DDLAnnotatedBuilder(ClassPool classPool) {
        pool = classPool;
    }

    @Override
    public Class<DDL> getAnnotationClass() {
        return DDL.class;
    }

    @Override
    public void buildMethod(CtClass accessHelper, Method m) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final String[] ddls = m.getAnnotation(getAnnotationClass()).value();

        final String methodName = m.getName();
        final Class<?> returnType = m.getReturnType();

        final StringBuilder body = new StringBuilder("{\n");

        if (log.isDebugEnabled()) {
            log.debug("Generate DDL method " + m);
        }

        if (!returnType.equals(void.class)) {
            throw new StorageSetupException("Invalid return type for updater in method " + methodName + "(): " + returnType.getName());
        }

        if (ArrayUtils.isNotEmpty(m.getParameterTypes())) {
            throw new StorageSetupException("DDL method " + methodName + " should be declared without parameters");
        }

        body.append("java.lang.String sql = null;\n");
        body.append("try {\n");
        body.append(BuilderUtils.getName(Connection.class));
        body.append(" con = this.factory.getConnection();\n");
        body.append("try {\n");
        body.append(BuilderUtils.getName(Statement.class));
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
        body.append(BuilderUtils.getName(SQLException.class));
        body.append(
                " e) {\n" +
                        "throw new "
        );
        body.append(BuilderUtils.getName(StorageException.class));
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
