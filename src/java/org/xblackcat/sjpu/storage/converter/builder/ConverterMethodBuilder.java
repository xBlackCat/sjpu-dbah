package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.builder.BuilderUtils;
import org.xblackcat.sjpu.builder.GeneratorException;
import org.xblackcat.sjpu.storage.typemap.TypeMapper;

import java.lang.reflect.Constructor;

class ConverterMethodBuilder {
    private final TypeMapper typeMapper;
    private final Constructor<?>[] constructors;
    private int idx = 1;
    private int shift = 1;

    protected ConverterMethodBuilder(TypeMapper typeMapper, Constructor<?>... constructors) {
        this.typeMapper = typeMapper;
        this.constructors = constructors;
    }

    protected String buildBody() throws GeneratorException {
        StringBuilder body = new StringBuilder("{\n");
        String newObject = initializeObject(body, constructors[0]);

        body.append("\nreturn ");
        body.append(newObject);
        body.append(";\n}");
        return body.toString();
    }

    protected String initializeObject(StringBuilder body, Constructor<?> constructor) throws GeneratorException {
        StringBuilder newObject = new StringBuilder("new ");
        newObject.append(BuilderUtils.getName(constructor.getDeclaringClass()));
        newObject.append("(\n");

        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        int i = 0;
        int parameterTypesLength = parameterTypes.length;
        while (i < parameterTypesLength) {
            Class<?> type = parameterTypes[i];
            i++;

            if (typeMapper.appendDeclaration(type, idx, body, newObject)) {
                idx++;
            } else {
                final Class<?> dbType = typeMapper.getDBTypeClass(type);

                if (shift >= constructors.length) {
                    throw new GeneratorException("Can't process type " + dbType.getName());
                }

                final Constructor<?> subElement = constructors[shift];
                if (!dbType.equals(subElement.getDeclaringClass())) {
                    throw new GeneratorException("Can't process type " + dbType.getName());
                }
                shift++;

                newObject.append(initializeObject(body, subElement));
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
