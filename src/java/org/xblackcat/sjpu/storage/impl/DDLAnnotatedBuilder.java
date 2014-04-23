package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.ann.DDL;
import org.xblackcat.sjpu.storage.skel.BuilderUtils;
import org.xblackcat.sjpu.storage.skel.IMethodBuilder;

import java.lang.reflect.Method;

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
            throw new StorageSetupException(
                    "Invalid return type for updater in method " +
                            methodName +
                            "(...): " +
                            returnType.getName()
            );
        }

        for (String sql : ddls) {
            body.append("helper.update(\"");
            body.append(StringEscapeUtils.escapeJava(sql));
            body.append("\");\n");
        }

        body.append("}");

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
