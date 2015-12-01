package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.ArrayUtils;
import org.xblackcat.sjpu.skel.BuilderUtils;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.ann.CloseResources;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 11.03.13 13:18
 *
 * @author xBlackCat
 */
class CloseResourcesAnnotatedBuilder extends AnAnnotatedMethodBuilder<CloseResources> {
    private final ClassPool pool;
    private final Class<? extends Annotation>[] checkAnnotationClasses;

    @SafeVarargs
    CloseResourcesAnnotatedBuilder(ClassPool classPool, Class<? extends Annotation>... checkAnnotationClasses) {
        super(CloseResources.class);
        pool = classPool;
        this.checkAnnotationClasses = checkAnnotationClasses;
    }

    @Override
    public void buildMethod(
            CtClass accessHelper,
            Class<?> targetClass,
            Method m
    ) throws NotFoundException, ReflectiveOperationException, CannotCompileException {
        final String methodName = m.getName();
        final Class<?> returnType = m.getReturnType();

        final StringBuilder body = new StringBuilder("{\ntry {\n");

        if (log.isDebugEnabled()) {
            log.debug("Generate CloseResources method " + m);
        }

        if (!returnType.equals(void.class)) {
            throw new GeneratorException("Invalid return type of method " + methodName + "(): " + returnType.getName());
        }

        if (ArrayUtils.isNotEmpty(m.getParameterTypes())) {
            throw new GeneratorException("CloseResources method " + methodName + " should be declared without parameters");
        }

        for (Method mm : targetClass.getMethods()) {
            if (isPollutingMethod(mm)) {
                cleanPollutingMethod(body, mm);
            }
        }

        for (Method mm : targetClass.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                // Already checked
                continue;
            }

            if (!Modifier.isAbstract(m.getModifiers())) {
                // Implemented methods are skipped from check
                continue;
            }

            if (isPollutingMethod(mm)) {
                cleanPollutingMethod(body, mm);
            }
        }

        body.append("} catch (");
        body.append(AHBuilderUtils.CN_java_sql_SQLException);
        body.append(
                " e) {\n" +
                        "new "
        );
        body.append(AHBuilderUtils.CN_StorageException);
        body.append(
                "(\"Failed to close resources\", e);\n" +
                        "} finally {\n" +
                        "try {\n" +
                        "con.close();\n" +
                        "} catch ("
        );
        body.append(AHBuilderUtils.CN_java_sql_SQLException);
        body.append(" ee) {\n" +
                            "new ");
        body.append(AHBuilderUtils.CN_StorageException);
        body.append(
                "(\"Failed to close connection\", ee);\n" +
                        "}\n" +
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

    private void cleanPollutingMethod(StringBuilder body, Method mm) {
        body.append("{\n // Close resources for method ");
        body.append(mm.toGenericString());
        body.append("\n\t");
        body.append(AHBuilderUtils.CN_java_sql_PreparedStatement);
        body.append(" stmt = ");
        body.append(BuilderUtils.asIdentifier(mm));
        body.append(";\n\tif (stmt != null) {\n\t\tstmt.close();\n\t}\n}\n");
    }

    private boolean isPollutingMethod(Method mm) {
        for (Class<? extends Annotation> a : checkAnnotationClasses) {
            if (mm.isAnnotationPresent(a)) {
                return true;
            }
        }

        return false;
    }

}
