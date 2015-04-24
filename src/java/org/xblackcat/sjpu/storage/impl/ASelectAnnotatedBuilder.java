package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.xblackcat.sjpu.skel.BuilderUtils;
import org.xblackcat.sjpu.skel.GeneratorException;
import org.xblackcat.sjpu.storage.consumer.IRowSetConsumer;
import org.xblackcat.sjpu.storage.converter.builder.ConverterInfo;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

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

    protected static Collection<ConverterInfo.Arg> substituteOptionalArgs(
            Collection<ConverterInfo.Arg> argumentList,
            List<Integer> optionalIndexes,
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
                initString = AHBuilderUtils.setParamValue(typeMap.getDbType(), value);
            } else {
                initString = AHBuilderUtils.setParamValue(type, "$" + idx);
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
