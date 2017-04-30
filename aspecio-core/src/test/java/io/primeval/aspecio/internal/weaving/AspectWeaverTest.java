package io.primeval.aspecio.internal.weaving;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.assertj.core.util.Lists;
import org.junit.BeforeClass;
import org.junit.Test;

import io.primeval.aspecio.aspect.interceptor.Advice;
import io.primeval.aspecio.aspect.interceptor.AdviceAdapter;
import io.primeval.aspecio.aspect.interceptor.BeforeAction;
import io.primeval.aspecio.aspect.interceptor.CallContext;
import io.primeval.aspecio.aspect.interceptor.Interceptor;
import io.primeval.aspecio.aspect.interceptor.arguments.Arguments;
import io.primeval.aspecio.internal.weaving.AspectWeaver;
import io.primeval.aspecio.internal.weaving.DynamicClassLoader;
import io.primeval.aspecio.internal.weaving.MethodIdentifier;
import io.primeval.aspecio.internal.weaving.WovenClassHolder;
import io.primeval.aspecio.internal.weaving.shared.Woven;
import io.primeval.aspecio.internal.weaving.testset.abstracts.AbstractSimplestService;
import io.primeval.aspecio.internal.weaving.testset.abstracts.AbstractedOverridingSimplestService;
import io.primeval.aspecio.internal.weaving.testset.abstracts.AbstractedSimplestService;
import io.primeval.aspecio.internal.weaving.testset.annotated.AnnotatedService;
import io.primeval.aspecio.internal.weaving.testset.api.BadValueException;
import io.primeval.aspecio.internal.weaving.testset.api.GenericInterface;
import io.primeval.aspecio.internal.weaving.testset.api.SimpleInterface;
import io.primeval.aspecio.internal.weaving.testset.api.SimplestInterface;
import io.primeval.aspecio.internal.weaving.testset.api.generic.GenericParamsService;
import io.primeval.aspecio.internal.weaving.testset.api.generic.GenericService;
import io.primeval.aspecio.internal.weaving.testset.bounds.BoundsImpl;
import io.primeval.aspecio.internal.weaving.testset.bounds.BoundsItf;
import io.primeval.aspecio.internal.weaving.testset.defaults.DefaultImpl;
import io.primeval.aspecio.internal.weaving.testset.defaults.DefaultItf;
import io.primeval.aspecio.internal.weaving.testset.defaults.DefaultOverridingImpl;
import io.primeval.aspecio.internal.weaving.testset.simpleservice.SimpleService;
import io.primeval.aspecio.internal.weaving.testset.simplest.SimplestService;

public final class AspectWeaverTest {

    private static DynamicClassLoader dynamicClassLoader;

    @BeforeClass
    public static void setUp() {
        dynamicClassLoader = new DynamicClassLoader(AspectWeaverTest.class.getClassLoader());
    }

    @Test
    public void shouldWeaveSimplestClass() {
        SimplestService simplestService = new SimplestService();

        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, SimplestService.class,
                new Class[] { SimplestInterface.class });
        assertThat(SimplestInterface.class).isAssignableFrom(wovenClassHolder.wovenClass);

        Object wovenService = wovenClassHolder.weavingFactory.apply(simplestService);
        assertThat(wovenService).isInstanceOf(SimplestInterface.class);

        SimplestInterface wovenItf = (SimplestInterface) wovenService;
        try {
            System.clearProperty(SimplestService.PROP_NAME);
            wovenItf.foo();
            assertThat(System.getProperty(SimplestService.PROP_NAME)).isEqualTo("true");
        } finally {
            System.clearProperty(SimplestService.PROP_NAME);
        }
    }

    @Test
    public void shouldWeaveAbstractedSimplestClass() {
        AbstractedSimplestService simplestService = new AbstractedSimplestService();

        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, AbstractedSimplestService.class,
                new Class[] { SimplestInterface.class });
        assertThat(SimplestInterface.class).isAssignableFrom(wovenClassHolder.wovenClass);

        Object wovenService = wovenClassHolder.weavingFactory.apply(simplestService);
        assertThat(wovenService).isInstanceOf(SimplestInterface.class);

        SimplestInterface wovenItf = (SimplestInterface) wovenService;
        try {
            System.clearProperty(AbstractSimplestService.PROP_NAME);
            wovenItf.foo();
            assertThat(System.getProperty(AbstractSimplestService.PROP_NAME)).isEqualTo("true");
        } finally {
            System.clearProperty(AbstractSimplestService.PROP_NAME);
        }
    }

    @Test
    public void shouldWeaveAbstractedOverridingSimplestClass() {
        AbstractedOverridingSimplestService simplestService = new AbstractedOverridingSimplestService();

        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, AbstractedOverridingSimplestService.class,
                new Class[] { SimplestInterface.class });
        assertThat(SimplestInterface.class).isAssignableFrom(wovenClassHolder.wovenClass);

        Object wovenService = wovenClassHolder.weavingFactory.apply(simplestService);
        assertThat(wovenService).isInstanceOf(SimplestInterface.class);

        SimplestInterface wovenItf = (SimplestInterface) wovenService;
        try {
            System.clearProperty(AbstractedOverridingSimplestService.PROP_NAME);
            wovenItf.foo();
            assertThat(System.getProperty(AbstractedOverridingSimplestService.PROP_NAME)).isEqualTo("true");
        } finally {
            System.clearProperty(AbstractedOverridingSimplestService.PROP_NAME);
        }
    }

    @Test
    public void shouldWeaveDefaultClass() {
        DefaultImpl simplestService = new DefaultImpl();

        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, DefaultImpl.class,
                new Class[] { DefaultItf.class });
        assertThat(DefaultItf.class).isAssignableFrom(wovenClassHolder.wovenClass);

        Object wovenService = wovenClassHolder.weavingFactory.apply(simplestService);
        assertThat(wovenService).isInstanceOf(DefaultItf.class);

        DefaultItf wovenItf = (DefaultItf) wovenService;
        Class<?> myDefault = wovenItf.myDefault();
        assertThat(myDefault).isSameAs(wovenService.getClass());

    }

    @Test
    public void shouldWeaveOverridedDefaultClass() {
        DefaultOverridingImpl simplestService = new DefaultOverridingImpl();

        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, DefaultOverridingImpl.class,
                new Class[] { DefaultItf.class });
        assertThat(DefaultItf.class).isAssignableFrom(wovenClassHolder.wovenClass);

        Woven wovenService = wovenClassHolder.weavingFactory.apply(simplestService);
        assertThat(wovenService).isInstanceOf(DefaultItf.class);

        DefaultItf wovenItf = (DefaultItf) wovenService;
        Class<?> myDefault = wovenItf.myDefault();
        assertThat(myDefault).isSameAs(DefaultOverridingImpl.class);
    }

    @Test
    public void shouldWeaveSimpleClass() throws IOException, BadValueException {
        SimpleService simpleService = new SimpleService();

        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, SimpleService.class,
                new Class[] { SimpleInterface.class });
        assertThat(SimpleInterface.class).isAssignableFrom(wovenClassHolder.wovenClass);

        Woven wovenService = wovenClassHolder.weavingFactory.apply(simpleService);

        wovenService.setInterceptor(new Interceptor() {
            @Override
            public Advice onCall(CallContext callContext) {
                if (callContext.method.getName().equals("increase")) {
                    String firstArg = callContext.parameters.get(0).getName();
                    return new AdviceAdapter() {
                        @Override
                        public BeforeAction initialAction() {
                            return BeforeAction.REQUEST_ARGUMENTS;
                        }

                        public BeforeAction visitArguments(Arguments arguments) {
                            if (arguments.intArg(firstArg) < 0) {
                                return BeforeAction.UPDATE_ARGUMENTS_AND_PROCEED;
                            }
                            return BeforeAction.PROCEED;
                        };

                        public Arguments updateArguments(Arguments arguments) {
                            return arguments.updater().setIntArg(firstArg, Math.abs(arguments.intArg(firstArg))).update();
                        };

                        public int afterPhases() {
                            return CallReturn.PHASE;
                        };

                        public int onIntReturn(int result) {
                            return result * 3;
                        };

                    };
                } else
                    return Advice.DEFAULT;
            }
        });

        assertThat(wovenService).isInstanceOf(SimpleInterface.class);

        SimpleInterface wovenItf = (SimpleInterface) wovenService;

        assertThat(simpleService.increase(10)).isEqualTo(20);
        assertThat(wovenItf.increase(10)).isEqualTo(60);

        assertThat(wovenItf.hello()).isEqualTo(simpleService.hello());

        assertThat(wovenItf.times()).isEqualTo(simpleService.times());

        assertThat(extractFromPrintStream(ps -> wovenItf.sayHello(ps))).isEqualTo(extractFromPrintStream(ps -> simpleService.sayHello(ps)));
    }

    private String extractFromPrintStream(Consumer<PrintStream> psConsumer) throws UnsupportedEncodingException, IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos)) {
            psConsumer.accept(ps);
            return baos.toString("UTF-8");
        }
    }

    @Test
    public void shouldWeaveAnnotations()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, AnnotatedService.class, new Class[0]);

        assertThat(wovenClassHolder.wovenClass.getAnnotations()).containsExactly(AnnotatedService.class.getAnnotations());

        AnnotatedService annotatedService = new AnnotatedService();
        Object wovenService = wovenClassHolder.weavingFactory.apply(annotatedService);

        Method method = AnnotatedService.class.getMethod("someMethod");
        Method wovenMethod = wovenClassHolder.wovenClass.getMethod("someMethod");

        assertThat(wovenMethod.invoke(wovenService)).isEqualTo(method.invoke(annotatedService));
        assertThat(wovenMethod.getAnnotations()).containsExactly(method.getAnnotations());
    }

    @Test
    public void shouldWeaveTwice() {
        AspectWeaver.weave(dynamicClassLoader, SimpleService.class, new Class[] { SimpleInterface.class });

        AspectWeaver.weave(dynamicClassLoader, SimpleService.class, new Class[] { SimpleInterface.class });

    }

    @Test
    public void shouldWeaveGenericServices() {
        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, GenericService.class,
                new Class<?>[] { GenericInterface.class });

        assertThat(GenericInterface.class).isAssignableFrom(wovenClassHolder.wovenClass);

        assertThat(wovenClassHolder.wovenClass.getGenericInterfaces())
                .containsExactly(GenericService.class.getGenericInterfaces());

        GenericService genericService = new GenericService();
        List<Object> mutableObjects = genericService.getObjects();

        @SuppressWarnings("unchecked")
        GenericInterface<Object, String> wovenService = (GenericInterface<Object, String>) wovenClassHolder.weavingFactory
                .apply(genericService);

        String someB = wovenService.makeB();
        assertThat(someB).isEqualTo(genericService.makeB());
        wovenService.doSome();
        assertThat(mutableObjects).containsExactly(someB);
    }

    @Test
    public void shouldWeaveBoundedClass() throws Exception {
        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, BoundsImpl.class, new Class<?>[] { BoundsItf.class });

        assertThat(BoundsItf.class).isAssignableFrom(wovenClassHolder.wovenClass);

        assertThat(wovenClassHolder.wovenClass.getGenericInterfaces())
                .containsExactly(BoundsImpl.class.getGenericInterfaces());

        ArrayList<MethodIdentifier> methodsToCompare = Lists.newArrayList(new MethodIdentifier("someList1"),
                new MethodIdentifier("someList2"), new MethodIdentifier("someList3"), new MethodIdentifier("someList4"),
                new MethodIdentifier("singleton", Number.class), new MethodIdentifier("foo", Number.class),
                new MethodIdentifier("makeFoo", Supplier.class));
        for (MethodIdentifier methodId : methodsToCompare) {
            Method method = BoundsImpl.class.getMethod(methodId.name, methodId.parameterTypes);
            Method wovenMethod = wovenClassHolder.wovenClass.getMethod(methodId.name, methodId.parameterTypes);

            assertThat(wovenMethod.getAnnotations()).containsExactly(method.getAnnotations());

            String[] methodParameterNames = Stream.of(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
            String[] wovenMethodParameterNames = Stream.of(wovenMethod.getParameters()).map(Parameter::getName).toArray(String[]::new);

            assertThat(wovenMethodParameterNames).containsExactly(methodParameterNames);
            assertThat(wovenMethod.getParameterTypes()).containsExactly(method.getParameterTypes());
            assertThat(wovenMethod.getGenericParameterTypes()).usingElementComparator(TestUtils.TYPE_COMPARATOR)
                    .containsExactly(method.getGenericParameterTypes());
            assertThat(wovenMethod.getParameterAnnotations()).containsExactly(method.getParameterAnnotations());
            assertThat(wovenMethod.getReturnType()).isEqualTo(method.getReturnType());
            assertThat(wovenMethod.getGenericReturnType()).usingComparator(TestUtils.TYPE_COMPARATOR)
                    .isEqualTo(method.getGenericReturnType());
            assertThat(wovenMethod.getExceptionTypes()).isEqualTo(method.getExceptionTypes());
            String[] wovenGenericExceptionTypes = Stream.of(wovenMethod.getGenericExceptionTypes()).map(java.lang.reflect.Type::getTypeName)
                    .toArray(String[]::new);
            String[] genericExceptionTypes = Stream.of(method.getGenericExceptionTypes()).map(java.lang.reflect.Type::getTypeName)
                    .toArray(String[]::new);
            assertThat(wovenGenericExceptionTypes).isEqualTo(genericExceptionTypes);
        }

    }

    @Test
    public void shouldWeaveMethodParameters() throws Exception {
        WovenClassHolder wovenClassHolder = AspectWeaver.weave(dynamicClassLoader, GenericParamsService.class, new Class<?>[0]);

        String[] wovenTypeParameters = Stream.of(wovenClassHolder.wovenClass.getTypeParameters()).map(TypeVariable::getName)
                .toArray(String[]::new);
        String[] typeParameters = Stream.of(GenericParamsService.class.getTypeParameters()).map(TypeVariable::getName)
                .toArray(String[]::new);

        assertThat(wovenTypeParameters).containsExactly(typeParameters);

        ArrayList<MethodIdentifier> methodsToCompare = Lists.newArrayList(new MethodIdentifier("myMethod", Consumer.class),
                new MethodIdentifier("fooMaker"), new MethodIdentifier("unsafe"), new MethodIdentifier("unsafeGeneric"),
                new MethodIdentifier("consumeA", Object.class));
        for (MethodIdentifier methodId : methodsToCompare) {
            Method method = GenericParamsService.class.getMethod(methodId.name, methodId.parameterTypes);
            Method wovenMethod = wovenClassHolder.wovenClass.getMethod(methodId.name, methodId.parameterTypes);

            assertThat(wovenMethod.getAnnotations()).containsExactly(method.getAnnotations());

            String[] methodParameterNames = Stream.of(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
            String[] wovenMethodParameterNames = Stream.of(wovenMethod.getParameters()).map(Parameter::getName).toArray(String[]::new);

            assertThat(wovenMethodParameterNames).containsExactly(methodParameterNames);
            assertThat(wovenMethod.getParameterTypes()).containsExactly(method.getParameterTypes());
            assertThat(wovenMethod.getGenericParameterTypes()).containsExactly(method.getGenericParameterTypes());
            assertThat(wovenMethod.getParameterAnnotations()).containsExactly(method.getParameterAnnotations());
            assertThat(wovenMethod.getReturnType()).isEqualTo(method.getReturnType());
            assertThat(wovenMethod.getGenericReturnType()).isEqualTo(method.getGenericReturnType());
            assertThat(wovenMethod.getExceptionTypes()).isEqualTo(method.getExceptionTypes());
            String[] wovenGenericExceptionTypes = Stream.of(wovenMethod.getGenericExceptionTypes()).map(java.lang.reflect.Type::getTypeName)
                    .toArray(String[]::new);
            String[] genericExceptionTypes = Stream.of(method.getGenericExceptionTypes()).map(java.lang.reflect.Type::getTypeName)
                    .toArray(String[]::new);
            assertThat(wovenGenericExceptionTypes).isEqualTo(genericExceptionTypes);
        }

    }
}
