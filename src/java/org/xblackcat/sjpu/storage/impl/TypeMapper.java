package org.xblackcat.sjpu.storage.impl;

import javassist.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.typemap.IMapFactory;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 17.12.13 14:51
 *
 * @author xBlackCat
 */
class TypeMapper {
    private final static AtomicInteger INSTANCES_AMOUNT = new AtomicInteger(0);
    private static final Log log = LogFactory.getLog(TypeMapper.class);

    private final IMapFactory[] mappers;
    private final int mapperId;

    private final Map<Class<?>, ITypeMap<?, ?>> initializedMappers = new HashMap<>();

    public TypeMapper(IMapFactory<?, ?>... mappers) {
        mapperId = INSTANCES_AMOUNT.getAndIncrement();
        this.mappers = mappers;
    }

    Class<? extends IToObjectConverter<?>> getTypeMapperConverter(
            ClassPool pool,
            Class<?> type
    ) throws NotFoundException, CannotCompileException, ReflectiveOperationException {
        ITypeMap<?, ?> typeMap = hasTypeMap(type);
        if (typeMap == null) {
            return null;
        }

        final String converterCN = getTypeMapConverterRef(typeMap.getClass());
        final String realClassName = typeMap.getRealType().getName();
        final String typeMapperRef = getTypeMapInstanceRef(typeMap.getRealType());
        try {

            if (log.isTraceEnabled()) {
                log.trace("Check if the converter already exists for mapper " + realClassName);
            }

            final String converterFQN = getClass().getName() + "$" + converterCN;
            final Class<?> aClass = Class.forName(converterFQN);

            if (IToObjectConverter.class.isAssignableFrom(aClass)) {
                if (log.isTraceEnabled()) {
                    log.trace("Converter class already exists: " + converterFQN);
                }
                return (Class<IToObjectConverter<?>>) aClass;
            } else {
                throw new StorageSetupException(
                        converterFQN + " class is already exists and it is not implements " + IToObjectConverter.class.getName()
                );
            }
        } catch (ClassNotFoundException ignore) {
            // Just build a new class
        }

        if (log.isTraceEnabled()) {
            log.trace("Build type map converter class for type " + realClassName);
        }

        StringBuilder body = new StringBuilder("{\nreturn (");
        final Class<?> returnType = typeMap.getRealType();
        body.append(BuilderUtils.getName(returnType));
        body.append(") ");
        body.append(typeMapperRef);
        body.append(".forRead(\n");

        Class<?> dbType = typeMap.getDbType();
        if (String.class.equals(dbType)) {
            body.append("$1.getString(1)");
        } else if (long.class.equals(dbType) || Long.class.equals(dbType)) {
            body.append("$1.getLong(1)");
        } else if (int.class.equals(dbType) || Integer.class.equals(dbType)) {
            body.append("$1.getInt(1)");
        } else if (short.class.equals(dbType) || Short.class.equals(dbType)) {
            body.append("$1.getShort(1)");
        } else if (byte.class.equals(dbType) || Byte.class.equals(dbType)) {
            body.append("$1.getByte(1)");
        } else if (boolean.class.equals(dbType) || Boolean.class.equals(dbType)) {
            body.append("$1.getBoolean(1)");
        } else if (byte[].class.equals(dbType)) {
            body.append("$1.getBytes(1)");
        } else if (Date.class.equals(dbType)) {
            body.append("$1.getTimeStamp(1)");
        } else {
            throw new StorageSetupException("Can't process DB type " + dbType.getName());
        }

        body.append("\n);\n}");

        final CtClass baseCtClass = pool.get(getClass().getName());
        final CtClass toObjectConverter = baseCtClass.makeNestedClass(converterCN, true);

        toObjectConverter.addInterface(pool.get(IToObjectConverter.class.getName()));

        if (log.isTraceEnabled()) {
            log.trace(
                    "Generated convert method " +
                            returnType.getName() +
                            " convert(ResultSet $1) throws SQLException " +
                            body.toString()
            );
        }


        final CtMethod method = CtNewMethod.make(
                Modifier.PUBLIC | Modifier.FINAL,
                pool.get(Object.class.getName()),
                "convert",
                BuilderUtils.toCtClasses(pool, ResultSet.class),
                BuilderUtils.toCtClasses(pool, SQLException.class),
                body.toString(),
                toObjectConverter
        );

        toObjectConverter.addMethod(method);

        if (log.isTraceEnabled()) {
            log.trace("Initialize subclass with object converter instance");
        }

        final Class<IToObjectConverter<?>> converterClass = (Class<IToObjectConverter<?>>) toObjectConverter.toClass();
        toObjectConverter.defrost();
        return converterClass;
    }

    private String getTypeMapConverterRef(Class<? extends ITypeMap> typeMap) {
        return "ToObjectTypeMapConverter_" + mapperId + "_" + StringUtils.replaceChars(BuilderUtils.getName(typeMap), '.', '_');
    }

    public ITypeMap<?, ?> hasTypeMap(Class<?> objClass) {
        if (initializedMappers.containsKey(objClass)) {
            // Already checked classes are here with 'null' as type mappers
            return initializedMappers.get(objClass);
        }

        for (IMapFactory<?, ?> typeMapper : mappers) {
            if (typeMapper.isAccepted(objClass)) {
                final ITypeMap<?, ?> typeMap = typeMapper.mapper((Class) objClass);

                ClassPool pool = ClassPool.getDefault();
                final String className = "TypeMap_" + mapperId + "_Instance";

                try {
                    final CtClass toObjectClazz = pool.get(objClass.getName());
                    CtClass instanceClass = toObjectClazz.makeNestedClass(className, true);
                    CtField instanceField = CtField.make(
                            "public static " + BuilderUtils.getName(ITypeMap.class) + " I;",
                            instanceClass
                    );
                    instanceClass.addField(instanceField, CtField.Initializer.byExpr("null"));

                    final Class aClass = instanceClass.toClass();
                    instanceClass.defrost();
                    final Field field = aClass.getField("I");
                    field.set(null, typeMap);
                } catch (NotFoundException | CannotCompileException | NoSuchFieldException | IllegalAccessException e) {
                    throw new StorageSetupException("Can't create instance of type mapper " + typeMap.getClass(), e);
                }

                initializedMappers.put(objClass, typeMap);
                return typeMap;
            }
        }

        initializedMappers.put(objClass, null);

        return null;
    }

    public String getTypeMapInstanceRef(Class<?> clazz) {
        return BuilderUtils.getName(clazz) + "." + nestedClassName() + ".I";
    }

    private String nestedClassName() {
        return "TypeMap_" + mapperId + "_Instance";
    }
}
