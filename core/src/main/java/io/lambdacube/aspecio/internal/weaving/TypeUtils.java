package io.lambdacube.aspecio.internal.weaving;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

import org.objectweb.asm.Type;

public final class TypeUtils {

    private TypeUtils() {
    }

    // We probably don't support everything, for instance nested types...
    // But it's simple enough to add here.
    public static String getTypeSignature(Class<?> clazzToWeave) {
        boolean isGeneric = false;
        isGeneric = clazzToWeave.getTypeParameters().length > 0;
        java.lang.reflect.Type genericSuperclass = clazzToWeave.getGenericSuperclass();
        if (!isGeneric) {
            isGeneric = genericSuperclass instanceof ParameterizedType;
            for (java.lang.reflect.Type t : clazzToWeave.getGenericInterfaces()) {
                if (!isGeneric) {
                    isGeneric = t instanceof ParameterizedType;
                }
            }
        }
        if (!isGeneric) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        if (clazzToWeave.getTypeParameters().length > 0) {
            buf.append('<');
            for (TypeVariable<?> t : clazzToWeave.getTypeParameters()) {
                buf.append(TypeUtils.getDescriptorForJavaType(t, true));
            }
            buf.append('>');
        }
        buf.append(getDescriptorForJavaType(genericSuperclass));

        for (java.lang.reflect.Type t : clazzToWeave.getGenericInterfaces()) {
            buf.append(getDescriptorForJavaType(t));
        }
        String typeSig = buf.toString();
        return typeSig;
    }

    public static String getMethodSignature(Method method) {
        boolean isGeneric = false;
        java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
        isGeneric = genericReturnType instanceof ParameterizedType;
        for (java.lang.reflect.Type t : method.getGenericParameterTypes()) {
            if (!isGeneric) {
                isGeneric = t instanceof ParameterizedType | t instanceof TypeVariable<?>;
            }
        }
        java.lang.reflect.Type[] genericExceptionTypes = method.getGenericExceptionTypes();
        for (java.lang.reflect.Type t : genericExceptionTypes) {
            if (!isGeneric) {
                isGeneric = t instanceof ParameterizedType | t instanceof TypeVariable<?>;
            }
        }

        if (!isGeneric) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        buf.append('(');
        for (java.lang.reflect.Type t : method.getGenericParameterTypes()) {
            buf.append(getDescriptorForJavaType(t));
        }
        buf.append(')');
        buf.append(getDescriptorForJavaType(genericReturnType));

        if (genericExceptionTypes.length > 0) {
            buf.append('^');
            for (java.lang.reflect.Type t : method.getGenericExceptionTypes()) {
                buf.append(TypeUtils.getDescriptorForJavaType(t, false));
            }
        }

        String typeSig = buf.toString();
        return typeSig;
    }

    public static String getDescriptorForJavaType(java.lang.reflect.Type type) {
        return getDescriptorForJavaType(type, false);
    }

    public static String getDescriptorForJavaType(java.lang.reflect.Type type, boolean expandRefs) {

        if (type instanceof Class<?>) {
            Class<?> cls = (Class<?>) type;
            return Type.getDescriptor(cls);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            StringBuilder buf = new StringBuilder();
            String descriptor = Type.getDescriptor(rawType);
            buf.append(descriptor, 0, descriptor.length() - 1); // omit ";"
            buf.append('<');
            java.lang.reflect.Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                buf.append(getDescriptorForJavaType(actualTypeArguments[i]));
            }
            buf.append(">;");
            return buf.toString();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            java.lang.reflect.Type genericComponentType = genericArrayType.getGenericComponentType();
            return '[' + getDescriptorForJavaType(genericComponentType);

        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            String name = typeVariable.getName();
            if (!expandRefs) {
                return "T" + name + ";";
            }
            java.lang.reflect.Type[] bounds = typeVariable.getBounds();
            StringBuilder buf = new StringBuilder();
            buf.append(name);
            for (java.lang.reflect.Type t : bounds) {
                buf.append(':');
                buf.append(getDescriptorForJavaType(t));
            }
            return buf.toString();
        }
        throw new UnsupportedOperationException("unsupported reflection type: " + type.getClass());

    }
}
