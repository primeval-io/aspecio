package io.lambdacube.aspecio.internal.weaving;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public final class WovenClassGenerator {

    public static final String WOVEN_TARGET_CLASS_SUFFIX = "$Woven$";
    public static final Set<Class<?>> IRETURN_TYPES = new HashSet<>(
            Arrays.asList(new Class<?>[] { int.class, char.class, byte.class, short.class }));

    public static byte[] weave(Class<?> clazzToWeave, Class<?>[] interfaces, Method[] methods) throws Exception {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        FieldVisitor fv;
        MethodVisitor mv;

        String wovenClassDescriptor = Type.getDescriptor(clazzToWeave);
        String wovenClassInternalName = Type.getInternalName(clazzToWeave);

        String targetClassInternalName = wovenClassInternalName + WOVEN_TARGET_CLASS_SUFFIX;
        String targetClassDescriptor = makeTargetClassDescriptor(wovenClassDescriptor);

        // Managing superclasses would be nice, but what about non-empty super
        // constructors? :( General support is doomed...
        // Class<?> superclass = clazzToWeave.getSuperclass();
        // String superClassDescriptor = Type.getDescriptor(superclass);
        // String superClassInternalName = Type.getInternalName(superclass);

        String typeSignature = TypeUtils.getTypeSignature(clazzToWeave);
        String[] itfs = Stream.of(interfaces).map(Type::getInternalName).toArray(String[]::new);
        cw.visit(52, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, targetClassInternalName, typeSignature,
                "java/lang/Object", itfs);

        addTypeAnnotations(clazzToWeave, cw);

        // Delegate field
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "delegate", wovenClassDescriptor, null, null);
            fv.visitEnd();
        }

        // Constructor
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + wovenClassDescriptor + ")V", null, null);
            mv.visitParameter("delegate", 0);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, targetClassInternalName, "delegate", wovenClassDescriptor);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("this", targetClassDescriptor, null, l0, l3, 0);
            mv.visitLocalVariable("delegate", wovenClassDescriptor, null, l0, l3, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        // Methods
        for (Method method : methods) {
            writeMethod(clazzToWeave, wovenClassInternalName, wovenClassDescriptor, targetClassInternalName, targetClassDescriptor, cw, mv,
                    method);
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static String makeTargetClassDescriptor(String wovenClassDescriptor) {
        StringBuilder buf = new StringBuilder();
        buf.append(wovenClassDescriptor, 0, wovenClassDescriptor.length() - 1); // omit
                                                                                // ';'
        buf.append(WOVEN_TARGET_CLASS_SUFFIX);
        buf.append(';');
        return buf.toString();
    }

    private static void addTypeAnnotations(Class<?> clazzToWeave, ClassWriter cw) throws IllegalAccessException, InvocationTargetException {
        for (Annotation ann : clazzToWeave.getDeclaredAnnotations()) {
            Class<? extends Annotation> annotationType = ann.annotationType();

            AnnotationVisitor av0 = cw.visitAnnotation(Type.getDescriptor(annotationType), true);

            addAnnotationTree(av0, ann, annotationType);
            av0.visitEnd();
        }
    }

    private static void addMethodAnnotations(Method method, MethodVisitor mv) throws IllegalAccessException, InvocationTargetException {
        for (Annotation ann : method.getAnnotations()) {
            Class<? extends Annotation> annotationType = ann.annotationType();

            AnnotationVisitor av0 = mv.visitAnnotation(Type.getDescriptor(annotationType), true);

            addAnnotationTree(av0, ann, annotationType);
            av0.visitEnd();
        }
    }

    private static void addMethodParameterAnnotations(Method method, MethodVisitor mv)
            throws IllegalAccessException, InvocationTargetException {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation ann : annotations) {
                Class<? extends Annotation> annotationType = ann.annotationType();

                AnnotationVisitor av0 = mv.visitParameterAnnotation(i, Type.getDescriptor(annotationType), true);
                addAnnotationTree(av0, ann, annotationType);
                av0.visitEnd();
            }
        }
    }

    private static void addAnnotationTree(AnnotationVisitor av0, Annotation ann, Class<? extends Annotation> annotationType)
            throws IllegalAccessException, InvocationTargetException {
        Method[] annMethods = annotationType.getDeclaredMethods();
        for (Method m : annMethods) {
            Class<?> returnType = m.getReturnType();

            if (returnType.isArray()) {
                Class<?> compType = returnType.getComponentType();
                String compDesc = Type.getDescriptor(compType);
                AnnotationVisitor avArray = av0.visitArray(m.getName());
                Object[] arr = (Object[]) m.invoke(ann);
                for (Object comp : arr) {
                    addAnnotation(null, compType, compDesc, avArray, comp);
                }
                avArray.visitEnd();
            } else {
                addAnnotation(m.getName(), returnType, Type.getDescriptor(returnType), av0, m.invoke(ann));
            }
        }
    }

    private static void addAnnotation(String name, Class<?> annotationMethodType, String typeDesc, AnnotationVisitor av, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if (Enum.class.isAssignableFrom(annotationMethodType)) {
            av.visitEnum(name, typeDesc, value.toString());
        } else if (annotationMethodType.isAnnotation()) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annType = (Class<? extends Annotation>) annotationMethodType;
            AnnotationVisitor annotationVisitor = av.visitAnnotation(name, typeDesc);
            addAnnotationTree(annotationVisitor, (Annotation) value, annType);
            annotationVisitor.visitEnd();
        } else {
            av.visit(name, value);
        }
    }

    private static void writeMethod(Class<?> clazzToWeave, String wovenClassInternalName, String wovenClassDescriptor,
            String targetClassInternalName, String targetClassDescriptor, ClassWriter cw,
            MethodVisitor mv, Method method) throws IllegalAccessException, InvocationTargetException {
        String methodDescriptor = Type.getMethodDescriptor(method);
        String methodName = method.getName();
        String methodSignature = TypeUtils.getMethodSignature(method);
        String[] exceptionTypes = null;
        Class<?>[] exceptionsClasses = method.getExceptionTypes();
        if (exceptionsClasses.length != 0) {
            exceptionTypes = Stream.of(exceptionsClasses).map(Type::getInternalName).toArray(String[]::new);
        }

        mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, methodName, methodDescriptor, methodSignature, exceptionTypes);
        Parameter[] parameters = method.getParameters();
        for (Parameter param : parameters) {
            mv.visitParameter(param.getName(), 0);
        }
        addMethodAnnotations(method, mv);
        addMethodParameterAnnotations(method, mv);

        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, targetClassInternalName, "delegate", wovenClassDescriptor);
        for (int i = 1; i <= parameters.length; i++) {
            mv.visitVarInsn(ALOAD, i);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, wovenClassInternalName, methodName, methodDescriptor, false);
        Label l1 = new Label();
        mv.visitLabel(l1);

        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            mv.visitInsn(RETURN);
        } else if (IRETURN_TYPES.contains(returnType)) {
            mv.visitInsn(IRETURN);
        } else if (returnType == long.class) {
            mv.visitInsn(LRETURN);
        } else if (returnType == double.class) {
            mv.visitInsn(DRETURN);
        } else if (returnType == float.class) {
            mv.visitInsn(FRETURN);
        } else {
            mv.visitInsn(ARETURN);
        }
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLocalVariable("this", targetClassDescriptor, null, l0, l2, 0);
        int i = 1;
        for (Parameter param : parameters) {
            mv.visitLocalVariable(param.getName(), Type.getDescriptor(param.getType()), null, l0, l2, i);
            i++;
        }
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

}
