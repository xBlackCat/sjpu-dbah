package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.StorageSetupException;
import org.xblackcat.sjpu.storage.skel.BuilderUtils;
import org.xblackcat.sjpu.storage.typemap.ITypeMap;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 25.04.2014 12:58
 *
 * @author xBlackCat
 */
class ConverterMethodBuilder {
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

        synchronized (BuilderUtils.class) {
            READ_DECLARATIONS = Collections.unmodifiableMap(map);
        }
    }

    private final TypeMapper typeMapper;
    private final Constructor<?>[] constructors;
    private int idx = 0;
    private int shift = 1;

    protected ConverterMethodBuilder(TypeMapper typeMapper, Constructor<?>... constructors) {
        this.typeMapper = typeMapper;
        this.constructors = constructors;
    }

    protected String buildBody() throws StorageSetupException {
        StringBuilder body = new StringBuilder("{\n");
        String newObject = initializeObject(body, constructors[0]);

        body.append("\nreturn ");
        body.append(newObject);
        body.append(";\n}");
        return body.toString();
    }

    protected String initializeObject(StringBuilder body, Constructor<?> constructor) throws StorageSetupException {
        StringBuilder newObject = new StringBuilder("new ");
        newObject.append(BuilderUtils.getName(constructor.getDeclaringClass()));
        newObject.append("(\n");

        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        int i = 0;
        int parameterTypesLength = parameterTypes.length;
        while (i < parameterTypesLength) {
            Class<?> type = parameterTypes[i];
            i++;

            final ITypeMap<?, ?> typeMap = typeMapper.hasTypeMap(type);
            final Class<?> dbType;
            if (typeMap == null) {
                dbType = type;
            } else {
                dbType = typeMap.getDbType();
            }

            String declarationLine = READ_DECLARATIONS.get(dbType);
            if (declarationLine == null) {
                if (shift >= constructors.length) {
                    throw new StorageSetupException("Can't process type " + dbType.getName());
                }

                final Constructor<?> subElement = constructors[shift];
                if (!dbType.equals(subElement.getDeclaringClass())) {
                    throw new StorageSetupException("Can't process type " + dbType.getName());
                }
                shift++;

                newObject.append(initializeObject(body, subElement));
            } else {
                idx++;
                body.append(String.format(declarationLine, idx));

                if (typeMap != null) {
                    newObject.append("(");
                    newObject.append(BuilderUtils.getName(type));
                    newObject.append(") ");
                    newObject.append(typeMapper.getTypeMapInstanceRef(type));
                    newObject.append(".forRead(");
                }
                newObject.append("value");
                newObject.append(idx);
                if (typeMap != null) {
                    newObject.append(")");
                }
            }
            newObject.append(",\n");
        }

        if (parameterTypesLength > 0) {
            newObject.setLength(newObject.length() - 2);
        }

        newObject.append("\n)");
        return newObject.toString();
    }
}
