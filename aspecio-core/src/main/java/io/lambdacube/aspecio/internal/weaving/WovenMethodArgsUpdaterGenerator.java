package io.lambdacube.aspecio.internal.weaving;

import static io.lambdacube.aspecio.internal.weaving.TypeUtils.IRETURN_TYPES;
import static io.lambdacube.aspecio.internal.weaving.TypeUtils.getLoadCode;
import static io.lambdacube.aspecio.internal.weaving.TypeUtils.getReturnCode;
import static io.lambdacube.aspecio.internal.weaving.TypeUtils.getTypeSize;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Stream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class WovenMethodArgsUpdaterGenerator implements Opcodes {

    public static final String SUFFIX_START = "$argsUpFor$";

    public static String getName(Class<?> wovenParentClass, Method method) {
        String suffix = SUFFIX_START + method.getName();
        return wovenParentClass.getName() + suffix;
    }

    public static byte[] generateMethodArgsUpdater(Class<?> wovenParentClass, Method method) throws Exception {

        ClassWriter cw = new ClassWriter(0);

        String wovenClassDescriptor = Type.getDescriptor(wovenParentClass);
        String wovenClassInternalName = Type.getInternalName(wovenParentClass);

        String suffix = SUFFIX_START + method.getName();
        String selfClassInternalName = wovenClassInternalName + suffix;
        String selfClassDescriptor = makeSelfClassDescriptor(wovenClassDescriptor, suffix);

        String argsClassInternalName = wovenClassInternalName + WovenMethodArgsGenerator.SUFFIX_START + method.getName();

        String constDesc = Type.getMethodDescriptor(Type.VOID_TYPE,
                Stream.concat(Stream.of(List.class), Stream.of(method.getParameterTypes())).map(Type::getType)
                        .toArray(Type[]::new));

        cw.visit(52, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, selfClassInternalName, null, "java/lang/Object",
                new String[] {  "io/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater" });
        Parameter[] parameters = method.getParameters();

        generateFields(method, cw, parameters);
        generateConstructor(method, cw, selfClassInternalName, selfClassDescriptor, constDesc, parameters);
        generateHashCodeMethod(cw, selfClassInternalName, selfClassDescriptor, parameters);
        generateEqualsMethod(cw, selfClassInternalName, selfClassDescriptor, parameters);
        generateToStringMethod(cw, selfClassInternalName, selfClassDescriptor, parameters);


        generateUpdateMethod(cw, selfClassInternalName, selfClassDescriptor, argsClassInternalName, constDesc, parameters);

        generateParametersGetter(cw, selfClassInternalName, selfClassDescriptor);

        generateArgumentSetters(cw, selfClassInternalName, selfClassDescriptor, parameters);
        generateArgumentGetters(cw, selfClassInternalName, selfClassDescriptor, parameters);
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static void generateUpdateMethod(ClassWriter cw, String selfClassInternalName, String selfClassDescriptor,
            String argsClassInternalName,
            String constDesc, Parameter[] parameters) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "update", "()Lio/lambdacube/aspecio/aspect/interceptor/arguments/Arguments;", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, argsClassInternalName);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, selfClassInternalName, "parameters", "Ljava/util/List;");
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, selfClassInternalName, parameter.getName(), Type.getDescriptor(parameter.getType()));
        }
        mv.visitMethodInsn(INVOKESPECIAL, argsClassInternalName, "<init>", constDesc, false);
        mv.visitInsn(ARETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", selfClassDescriptor, null, l0, l1, 0);
        mv.visitMaxs(parameters.length + 3, 1);
        mv.visitEnd();
    }


    private static void generateFields(Method method, ClassWriter cw, Parameter[] parameters) {
        FieldVisitor fv;
        cw.visitSource(method.getName() + "ArgsUpdater@@aspecio", null);

        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL, "parameters", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/reflect/Parameter;>;",
                null);
        fv.visitEnd();

        for (Parameter p : parameters) {
            fv = cw.visitField(ACC_PUBLIC, p.getName(), Type.getDescriptor(p.getType()), null, null);
            fv.visitEnd();
        }
    }

    private static void generateConstructor(Method method, ClassWriter cw, String selfClassInternalName, String selfClassDescriptor,
            String constDesc, Parameter[] parameters) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", constDesc, null, null);
        mv.visitParameter("parameters", 0);
        int paramCount = parameters.length;
        int[] paramIndices = new int[paramCount];
        int nextVarIndex = 2; // 0 = "this", 1 = "parameters"
        for (int i = 0; i < paramCount; i++) {
            Parameter param = parameters[i];
            paramIndices[i] = nextVarIndex;
            mv.visitParameter(param.getName(), 0);
            nextVarIndex += getTypeSize(param.getType());
        }
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, selfClassInternalName, "parameters", "Ljava/util/List;");

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            mv.visitVarInsn(ALOAD, 0);
            Class<?> paramType = parameter.getType();
            mv.visitVarInsn(getLoadCode(paramType), paramIndices[i]);
            mv.visitFieldInsn(PUTFIELD, selfClassInternalName, parameter.getName(), Type.getDescriptor(paramType));
        }

        mv.visitInsn(RETURN);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitLocalVariable("this", selfClassDescriptor, null, l0, l7, 0);
        mv.visitLocalVariable("parameters", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/reflect/Parameter;>;", l0, l7, 1);

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            mv.visitLocalVariable(parameter.getName(), Type.getDescriptor(parameter.getType()), null, l0, l7, paramIndices[i]);
        }
        mv.visitMaxs(2, nextVarIndex);
        mv.visitEnd();
    }

    private static void generateHashCodeMethod(ClassWriter cw, String selfClassInternalName, String selfClassDescriptor,
            Parameter[] parameters) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
        mv.visitCode();

        // int result = 1;

        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, 1);

        for (Parameter param : parameters) {
            Class<?> type = param.getType();
            mv.visitIntInsn(BIPUSH, 31);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(IMUL);
            mv.visitVarInsn(ALOAD, 0);
            Type typeType = Type.getType(type);
            mv.visitFieldInsn(GETFIELD, selfClassInternalName, param.getName(), typeType.getDescriptor());
            if (type.isPrimitive()) {
                Class<?> boxed = TypeUtils.getBoxed(type);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(boxed), "hashCode",
                        Type.getMethodDescriptor(Type.INT_TYPE, typeType), false);

            } else {
                mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "hashCode", "(Ljava/lang/Object;)I", false);
            }
            mv.visitInsn(IADD);
            mv.visitVarInsn(ISTORE, 1);
        }

        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(IRETURN);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitLocalVariable("this", selfClassDescriptor, null, l0, l7, 0);
        mv.visitLocalVariable("result", "I", null, l0, l7, 1);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private static void generateEqualsMethod(ClassWriter cw, String selfClassInternalName, String selfClassDescriptor,
            Parameter[] parameters) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
        mv.visitParameter("obj", 0);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        Label l1 = new Label();
        mv.visitJumpInsn(IF_ACMPNE, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l1);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 1);
        Label l3 = new Label();
        mv.visitJumpInsn(IFNONNULL, l3);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l3);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        Label l5 = new Label();
        mv.visitJumpInsn(IF_ACMPEQ, l5);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l5);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, selfClassInternalName);
        mv.visitVarInsn(ASTORE, 2);
        Label l7 = new Label();
        mv.visitLabel(l7);
        Label l8 = new Label();

        for (Parameter param : parameters) {
            mv.visitVarInsn(ALOAD, 0);
            Class<?> type = param.getType();
            String paramName = param.getName();
            String typeDesc = Type.getDescriptor(type);
            mv.visitFieldInsn(GETFIELD, selfClassInternalName, paramName, typeDesc);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(GETFIELD, selfClassInternalName, paramName, Type.getDescriptor(type));
            if (type.isPrimitive()) {
                if (IRETURN_TYPES.contains(type)) {
                    mv.visitJumpInsn(IF_ICMPNE, l8);
                } else if (type == float.class) {
                    mv.visitInsn(FCMPL);
                    mv.visitJumpInsn(IFNE, l8);
                } else if (type == double.class) {
                    mv.visitInsn(DCMPL);
                    mv.visitJumpInsn(IFNE, l8);
                } else if (type == long.class) {
                    mv.visitInsn(LCMP);
                    mv.visitJumpInsn(IFNE, l8);
                }
            } else {
                mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
                mv.visitJumpInsn(IFEQ, l8);
            }
        }
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l8);
        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { selfClassInternalName }, 0, null);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        Label l9 = new Label();
        mv.visitLabel(l9);
        mv.visitLocalVariable("this", selfClassDescriptor, null, l0, l9, 0);
        mv.visitLocalVariable("obj", "Ljava/lang/Object;", null, l0, l9, 1);
        mv.visitLocalVariable("other", selfClassDescriptor, null, l7, l9, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }

    private static void generateToStringMethod(ClassWriter cw, String selfClassInternalName, String selfClassDescriptor,
            Parameter[] parameters) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(Type.getType(selfClassDescriptor));
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitLdcInsn(" [");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            mv.visitLdcInsn(param.getName() + "=");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                    false);
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitFieldInsn(GETFIELD, selfClassInternalName, param.getName(), Type.getDescriptor(param.getType()));
            Class<?> paramType = param.getType();
            if (paramType.isPrimitive()) {
                // special case with StringBuilder, no specific method we default to append(int)
                if (paramType == short.class || paramType == byte.class) {
                    paramType = int.class;
                }
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.getType(paramType)), false);
            } else {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
            }
            if (i + 1 < parameters.length) {
                mv.visitLdcInsn(", ");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false);
            }
        }
        mv.visitLdcInsn("]");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitInsn(ARETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", selfClassDescriptor, null, l0, l1, 0);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
    }

    private static void generateParametersGetter(ClassWriter cw, String selfClassInternalName, String selfClassDescriptor) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "parameters", "()Ljava/util/List;", "()Ljava/util/List<Ljava/lang/reflect/Parameter;>;", null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, selfClassInternalName, "parameters", "Ljava/util/List;");
        mv.visitInsn(ARETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", selfClassDescriptor, null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private static void generateArgumentSetters(ClassWriter cw, String selfClassInternalName, String selfClassDescriptor,
            Parameter[] parameters) {
        MethodVisitor mv;
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setObjectArg",
                    "(Ljava/lang/String;Ljava/lang/Object;)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;",
                    "<T:Ljava/lang/Object;>(Ljava/lang/String;TT;)V", null);
            generateArgSetterCode(Object.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setIntArg",
                    "(Ljava/lang/String;I)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(int.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setShortArg",
                    "(Ljava/lang/String;S)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(short.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setLongArg",
                    "(Ljava/lang/String;J)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(long.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setByteArg",
                    "(Ljava/lang/String;B)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(byte.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setBooleanArg",
                    "(Ljava/lang/String;Z)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(boolean.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setFloatArg",
                    "(Ljava/lang/String;F)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(float.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setDoubleArg",
                    "(Ljava/lang/String;D)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(double.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setCharArg",
                    "(Ljava/lang/String;C)Lio/lambdacube/aspecio/aspect/interceptor/arguments/ArgumentsUpdater;", null, null);
            generateArgSetterCode(char.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
    }

    private static void generateArgSetterCode(Class<?> argType, MethodVisitor mv, String selfClassInternalName, String selfClassDescriptor,
            Parameter[] parameters) {
        Parameter[] matchingParams = Stream.of(parameters).filter(c -> argType.isAssignableFrom(c.getType())).toArray(Parameter[]::new);
        mv.visitParameter("argName", 0);
        mv.visitParameter("newValue", 0);
        mv.visitCode();
        Label first = new Label();
        Label nextLabel = first;
        for (int i = 0; i < matchingParams.length; i++) {
            Parameter parameter = matchingParams[i];
            mv.visitLabel(nextLabel);
            if (i > 0) {
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(parameter.getName());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            nextLabel = new Label();
            mv.visitJumpInsn(IFEQ, nextLabel);
            Label l2 = new Label();
            mv.visitLabel(l2);
            Class<?> paramType = parameter.getType();
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitVarInsn(getLoadCode(argType), 2);
            if (Object.class == argType) {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(paramType));
            }
            mv.visitFieldInsn(PUTFIELD, selfClassInternalName, parameter.getName(), Type.getDescriptor(paramType));
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ARETURN);
        }
        // final else
        mv.visitLabel(nextLabel);
        if (matchingParams.length > 0) {
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        genExceptionThrowForUnknownParam(argType.getSimpleName(), mv);
        Label last = new Label();
        mv.visitLabel(last);
        mv.visitLocalVariable("this", selfClassDescriptor, null, first, last, 0);
        mv.visitLocalVariable("argName", "Ljava/lang/String;", null, first, last, 1);
        mv.visitLocalVariable("newValue", Type.getDescriptor(argType), argType == Object.class ? "TT;" : null, first, last, 2);
        mv.visitMaxs(5, 2 + getTypeSize(argType));
        mv.visitEnd();
    }

    private static void generateArgumentGetters(ClassWriter cw, String selfClassInternalName, String selfClassDescriptor,
            Parameter[] parameters) {
        MethodVisitor mv;
        {
            mv = cw.visitMethod(ACC_PUBLIC, "objectArg", "(Ljava/lang/String;)Ljava/lang/Object;",
                    "<T:Ljava/lang/Object;>(Ljava/lang/String;)TT;", null);
            generateArgGetterCode(Object.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "intArg", "(Ljava/lang/String;)I", null, null);
            generateArgGetterCode(int.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "shortArg", "(Ljava/lang/String;)S", null, null);
            generateArgGetterCode(short.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "longArg", "(Ljava/lang/String;)J", null, null);
            generateArgGetterCode(long.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "byteArg", "(Ljava/lang/String;)B", null, null);
            generateArgGetterCode(byte.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "booleanArg", "(Ljava/lang/String;)Z", null, null);
            generateArgGetterCode(boolean.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "floatArg", "(Ljava/lang/String;)F", null, null);
            generateArgGetterCode(float.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "doubleArg", "(Ljava/lang/String;)D", null, null);
            generateArgGetterCode(double.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "charArg", "(Ljava/lang/String;)C", null, null);
            generateArgGetterCode(char.class, mv, selfClassInternalName, selfClassDescriptor, parameters);
        }
    }

    private static void generateArgGetterCode(Class<?> argType, MethodVisitor mv, String selfClassInternalName, String selfClassDescriptor,
            Parameter[] parameters) {
        Parameter[] matchingParams = Stream.of(parameters).filter(c -> argType.isAssignableFrom(c.getType())).toArray(Parameter[]::new);
        mv.visitParameter("argName", 0);
        mv.visitCode();
        Label first = new Label();
        Label nextLabel = first;
        for (int i = 0; i < matchingParams.length; i++) {
            Parameter parameter = matchingParams[i];
            mv.visitLabel(nextLabel);
            if (i > 0) {
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(parameter.getName());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            nextLabel = new Label();
            mv.visitJumpInsn(IFEQ, nextLabel);
            Label l2 = new Label();
            mv.visitLabel(l2);
            Class<?> paramType = parameter.getType();
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitFieldInsn(GETFIELD, selfClassInternalName, parameter.getName(), Type.getDescriptor(paramType));
            mv.visitInsn(getReturnCode(paramType));
        }
        // final else
        mv.visitLabel(nextLabel);
        if (matchingParams.length > 0) {
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        genExceptionThrowForUnknownParam(argType.getSimpleName(), mv);
        Label last = new Label();
        mv.visitLabel(last);
        mv.visitLocalVariable("this", selfClassDescriptor, null, first, last, 0);
        mv.visitLocalVariable("argName", "Ljava/lang/String;", null, first, last, 1);

        mv.visitMaxs(5, 2);
        mv.visitEnd();
    }

    private static void genExceptionThrowForUnknownParam(String paramType, MethodVisitor mv) {
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("No " + paramType + " parameter named ");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }

    private static String makeSelfClassDescriptor(String wovenClassDescriptor, String suffix) {
        StringBuilder buf = new StringBuilder();
        buf.append(wovenClassDescriptor, 0, wovenClassDescriptor.length() - 1); // omit
                                                                                // ';'
        buf.append(suffix);
        buf.append(';');
        return buf.toString();
    }
}
