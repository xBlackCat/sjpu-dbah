package org.xblackcat.sjpu.storage.typemap;

import javassist.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;
import org.xblackcat.sjpu.storage.impl.AHBuilderUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 17.12.13 14:51
 *
 * @author xBlackCat
 */
public class TypeMapper {
    static final Map<Class<?>, String> READ_DECLARATIONS;

    static {
        Map<Class<?>, String> map = new HashMap<>();

        // Integer types
        map.put(long.class, "long value%1$d = $1.getLong(%1$d);\n");
        map.put(
                Long.class,
                "long tmp%1$d = $1.getLong(%1$d);\njava.lang.Long value%1$d = $1.wasNull() ? null : java.lang.Long.valueOf(tmp%1$d);\n"
        );
        map.put(int.class, "int value%1$d = $1.getInt(%1$d);\n");
        map.put(
                Integer.class,
                "int tmp%1$d = $1.getInt(%1$d);\njava.lang.Integer value%1$d = $1.wasNull() ? null : java.lang.Integer.valueOf(tmp%1$d);\n"
        );
        map.put(short.class, "short value%1$d = $1.getShort(%1$d);\n");
        map.put(
                Short.class,
                "short tmp%1$d = $1.getShort(%1$d);\njava.lang.Short value%1$d = $1.wasNull() ? null : java.lang.Short.valueOf(tmp%1$d);\n"
        );
        map.put(byte.class, "byte value%1$d = $1.getByte(%1$d);\n");
        map.put(
                Byte.class,
                "byte tmp%1$d = $1.getByte(%1$d);\njava.lang.Byte value%1$d = $1.wasNull() ? null : java.lang.Byte.valueOf(tmp%1$d);\n"
        );

        // Float types
        map.put(double.class, "double value%1$d = $1.getDouble(%1$d);\n");
        map.put(
                Double.class,
                "double tmp%1$d = $1.getDouble(%1$d);\njava.lang.Double value%1$d = $1.wasNull() ? null : java.lang.Double.valueOf(tmp%1$d);\n"
        );
        map.put(float.class, "float value%1$d = $1.getFloat(%1$d);\n");
        map.put(
                Float.class,
                "float tmp%1$d = $1.getFloat(%1$d);\njava.lang.Float value%1$d = $1.wasNull() ? null : java.lang.Float.valueOf(tmp%1$d);\n"
        );

        // Boolean type
        map.put(boolean.class, "boolean value%1$d = $1.getBoolean(%1$d);\n");
        map.put(
                Boolean.class,
                "boolean tmp%1$d = $1.getBoolean(%1$d);\njava.lang.Boolean value%1$d = $1.wasNull() ? null : java.lang.Boolean.valueOf(tmp%1$d);\n"
        );

        // Other types
        map.put(byte[].class, "byte[] value%1$d = $1.getBytes(%1$d);\n");
        map.put(String.class, String.class.getName() + " value%1$d = $1.getString(%1$d);\n");
        map.put(BigDecimal.class, BigDecimal.class.getName() + " value%1$d = $1.getBigDecimal(%1$d);\n");
        map.put(Object.class, Object.class.getName() + " value%1$d = $1.getObject(%1$d);\n");

        // Time classes
        map.put(
                java.sql.Time.class,
                java.sql.Time.class.getName() + " value%1$d = $1.getTime(%1$d);\n"
        );
        map.put(
                java.sql.Date.class,
                java.sql.Date.class.getName() + " value%1$d = $1.getDate(%1$d);\n"
        );
        map.put(
                java.sql.Timestamp.class,
                java.sql.Timestamp.class.getName() + " value%1$d = $1.getTimestamp(%1$d);\n"
        );
        map.put(
                java.sql.Array.class,
                java.sql.Array.class.getName() + " value%1$d = $1.getArray(%1$d);\n"
        );

        synchronized (AHBuilderUtils.class) {
            READ_DECLARATIONS = Collections.unmodifiableMap(map);
        }
    }

    private static final AtomicInteger INSTANCES_AMOUNT = new AtomicInteger(0);
    private static final Log log = LogFactory.getLog(TypeMapper.class);

    private final IMapFactory[] mappers;
    private final int mapperId;

    private final Map<Class<?>, ITypeMap<?, ?>> initializedMappers = new HashMap<>();
    private final ClassPool parentPool;

    public TypeMapper(ClassPool pool, IMapFactory<?, ?>... mappers) {
        this(pool, INSTANCES_AMOUNT.getAndIncrement(), mappers);
    }

    // For tests only
    protected TypeMapper(ClassPool pool, int id, IMapFactory<?, ?>... mappers) {
        parentPool = pool;
        mapperId = id;
        this.mappers = mappers;
    }

    public Class<?> getDBTypeClass(Class<?> type) {
        final ITypeMap<?, ?> typeMap = hasTypeMap(type);
        final Class<?> dbType;
        if (typeMap == null) {
            dbType = type;
        } else {
            dbType = typeMap.getDbType();
        }
        return dbType;
    }

    public Class<? extends IToObjectConverter<?>> getTypeMapperConverter(
            Class<?> type
    ) throws NotFoundException, CannotCompileException, ReflectiveOperationException {
        ITypeMap<?, ?> typeMap = hasTypeMap(type);
        if (typeMap == null) {
            return null;
        }

        final String converterCN = getTypeMapConverterRef(type);
        final Class<? extends TypeMapper> targetClass = getClass();
        return getOrInitTypeMap(type, () -> buildMapperCode(type, typeMap), converterCN, targetClass);
    }

    private String buildMapperCode(Class<?> type, ITypeMap<?, ?> typeMap) {
        StringBuilder body = new StringBuilder("{\n");
        StringBuilder bodyTail = new StringBuilder("return ");

        if (!appendDeclaration(type, 1, body, bodyTail)) {
            throw new GeneratorException("Can't process DB type " + typeMap.getDbType().getName());
        }

        body.append(bodyTail);
        body.append(";\n}");
        return body.toString();
    }

    public boolean appendDeclaration(Class<?> type, int idx, StringBuilder bodyHead, StringBuilder bodyTail) {
        final ITypeMap<?, ?> typeMap = hasTypeMap(type);
        final Class<?> dbType;
        if (typeMap == null) {
            dbType = type;
        } else {
            dbType = typeMap.getDbType();
        }

        String declarationLine = READ_DECLARATIONS.get(dbType);
        if (declarationLine == null) {
            return false;
        }

        bodyHead.append(String.format(declarationLine, idx));

        if (typeMap != null) {
            bodyTail.append("(");
            bodyTail.append(BuilderUtils.getName(type));
            bodyTail.append(") ");
            bodyTail.append(getTypeMapInstanceRef(type));
            bodyTail.append(".forRead(");
        }
        bodyTail.append("value");
        bodyTail.append(idx);
        if (typeMap != null) {
            bodyTail.append(")");
        }

        return true;
    }

    private String getTypeMapConverterRef(Class<?> type) {
        return "ToObjectTypeMapConverter_" + mapperId + "_" + BuilderUtils.asIdentifier(type);
    }

    public boolean canProcess(Class<?> objClass) {
        if (READ_DECLARATIONS.containsKey(objClass)) {
            return true;
        }

        final ITypeMap<?, ?> typeMap = initializedMappers.get(objClass);
        if (typeMap != null) {
            // Already checked classes are here with 'null' as type mappers
            return true;
        }

        for (IMapFactory<?, ?> typeMapper : mappers) {
            if (typeMapper.isAccepted(objClass)) {
                return true;
            }
        }

        return false;
    }

    public synchronized ITypeMap<?, ?> hasTypeMap(Class<?> objClass) {
        if (initializedMappers.containsKey(objClass)) {
            // Already checked classes are here with 'null' as type mappers
            return initializedMappers.get(objClass);
        }

        for (IMapFactory<?, ?> typeMapper : mappers) {
            if (typeMapper.isAccepted(objClass)) {
                @SuppressWarnings("unchecked")
                final ITypeMap<?, ?> typeMap = typeMapper.mapper((Class) objClass);

                ClassPool pool = BuilderUtils.getClassPool(parentPool, objClass);
                final String className = nestedClassName(objClass);

                try {
                    final CtClass toObjectClazz = pool.get(getClass().getName());
                    CtClass instanceClass = toObjectClazz.makeNestedClass(className, true);
                    CtField instanceField = CtField.make(
                            "public static " + AHBuilderUtils.CN_ITypeMap + " I;",
                            instanceClass
                    );
                    instanceClass.addField(instanceField, CtField.Initializer.byExpr("null"));

                    if (log.isTraceEnabled()) {
                        log.trace(
                                "Generate instance holder class for type mapper " + typeMap.getClass().getName() +
                                        " (toString: " + typeMap.toString() + ")."
                        );
                    }


                    final Class aClass = instanceClass.toClass();
                    instanceClass.defrost();
                    final Field field = aClass.getField("I");
                    field.set(null, typeMap);
                } catch (NotFoundException | CannotCompileException | NoSuchFieldException | IllegalAccessException e) {
                    throw new GeneratorException("Can't create instance of type mapper " + typeMap.getClass(), e);
                }

                initializedMappers.put(objClass, typeMap);
                return typeMap;
            }
        }

        initializedMappers.put(objClass, null);

        return null;
    }

    public String getTypeMapInstanceRef(Class<?> clazz) {
        return getClass().getName() + "." + nestedClassName(clazz) + ".I";
    }

    private String nestedClassName(Class<?> objClass) {
        return "TypeMap_" + BuilderUtils.asIdentifier(objClass) + "_" + mapperId + "_Instance";
    }

    public ClassPool getParentPool() {
        return parentPool;
    }

    public synchronized Class<IToObjectConverter<?>> getOrInitTypeMap(
            Class<?> type,
            Supplier<String> bodyCodeSupplier,
            String converterCN,
            Class<?> targetClass
    ) throws NotFoundException, CannotCompileException {
        try {

            if (log.isTraceEnabled()) {
                log.trace("Check if the converter already exists for class " + type.getName());
            }

            final String converterFQN = targetClass.getName() + "$" + converterCN;
            final Class<?> aClass = BuilderUtils.getClass(converterFQN, parentPool);

            if (IToObjectConverter.class.isAssignableFrom(aClass)) {
                if (log.isTraceEnabled()) {
                    log.trace("Converter class already exists: " + converterFQN);
                }
                @SuppressWarnings("unchecked")
                final Class<IToObjectConverter<?>> aClazz = (Class<IToObjectConverter<?>>) aClass;
                return aClazz;
            } else {
                throw new GeneratorException(
                        converterFQN + " class is already exists and it is not implements " + IToObjectConverter.class.getName()
                );
            }
        } catch (ClassNotFoundException ignore) {
            // Just build a new class
        }

        if (log.isTraceEnabled()) {
            log.trace("Build type map converter class for type " + type.getName());
        }

        String bodyCode = bodyCodeSupplier.get();
        final CtClass baseCtClass = parentPool.get(targetClass.getName());
        final CtClass toObjectConverter = baseCtClass.makeNestedClass(converterCN, true);

        toObjectConverter.addInterface(parentPool.get(IToObjectConverter.class.getName()));

        if (log.isTraceEnabled()) {
            log.trace("Generated convert method " + type.getName() + " convert(ResultSet $1) throws SQLException " + bodyCode);
        }


        final CtMethod method = CtNewMethod.make(
                Modifier.PUBLIC | Modifier.FINAL,
                parentPool.get(Object.class.getName()),
                "convert",
                BuilderUtils.toCtClasses(parentPool, ResultSet.class),
                BuilderUtils.toCtClasses(parentPool, SQLException.class),
                bodyCode,
                toObjectConverter
        );

        toObjectConverter.addMethod(method);

        if (log.isTraceEnabled()) {
            log.trace("Initialize subclass with object converter instance");
        }

        @SuppressWarnings("unchecked")
        final Class<IToObjectConverter<?>> converterClass = (Class<IToObjectConverter<?>>) toObjectConverter.toClass();
        toObjectConverter.defrost();
        return converterClass;
    }
}
