package io.lambdacube.aspecio.it;

import static io.lambdacube.aspecio.it.TestProvisioningConfig.baseOptions;
import static io.lambdacube.aspecio.it.TestProvisioningConfig.dsAndFriends;
import static io.lambdacube.aspecio.it.TestProvisioningConfig.slf4jLogging;
import static io.lambdacube.aspecio.it.TestProvisioningConfig.testingBundles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import io.lambdacube.aspecio.examples.DemoConsumer;
import io.lambdacube.aspecio.examples.aspect.counting.CountingAspect;
import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class AspecioIntegrationTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private DemoConsumer demoConsumer;

    public static Option exampleApplication() {
        return composite(dsAndFriends(),
                mavenBundle("com.google.guava", "guava").versionAsInProject(),
                mavenBundle("io.lambdacube.aspecio", "aspecio-examples").versionAsInProject());
    }

    @Configuration
    public Option[] config() throws Throwable {
        return new Option[] {
                baseOptions(),
                testingBundles(),
                slf4jLogging(),
                mavenBundle("io.lambdacube.aspecio", "aspecio-core").versionAsInProject(),
                exampleApplication(),
                frameworkStartLevel(START_LEVEL_TEST_BUNDLE)
        };
    }

    @Test
    public void someTest() throws Exception {
        ServiceTracker<Hello, Hello> helloTracker = new ServiceTracker<>(bundleContext, Hello.class, null);
        helloTracker.open();

        ServiceTracker<Goodbye, Goodbye> goodbyeTracker = new ServiceTracker<>(bundleContext, Goodbye.class, null);
        goodbyeTracker.open();

        helloTracker.waitForService(10_000L);
        helloTracker.waitForService(10_000L);

        // In our system, we have exactly one service, that is woven by Aspecio,
        // that provides both Hello and Goodbye.
        assertThat(helloTracker.getServiceReferences().length).isEqualTo(1);
        assertThat(goodbyeTracker.getServiceReferences().length).isEqualTo(1);

        ServiceReference<Hello> helloSr = helloTracker.getServiceReference();
        ServiceReference<Goodbye> goodbyeSr = goodbyeTracker.getServiceReference();

        // The following service references should be the same
        assertThat(helloSr).isEqualTo(goodbyeSr);

        ServiceReference<?> commonSr = helloSr;

        // Hidden property added to woven services
        Object wovenProperty = commonSr.getProperty(".service.aspect.woven");
        assertThat(wovenProperty).isNotNull().isInstanceOf(String[].class);
        assertThat((String[]) wovenProperty).containsExactly(CountingAspect.class.getName());

        Hello hello = helloTracker.getService();
        Goodbye goodbye = goodbyeTracker.getService();

        assertThat(hello).isSameAs(goodbye);
        assertThat(hello.getClass().getName()).isEqualTo("io.lambdacube.aspecio.examples.greetings.internal.HelloGoodbyeImpl$Woven$");

        helloTracker.close();
        goodbyeTracker.close();

        System.out.println(demoConsumer.getLongResult());

        assertThat(extractFromPrintStream(ps -> demoConsumer.consumeTo(ps))).isEqualTo("hello goodbye\n");
    }

    private String extractFromPrintStream(Consumer<PrintStream> psConsumer) throws UnsupportedEncodingException, IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos)) {
            psConsumer.accept(ps);
            return baos.toString("UTF-8");
        }
    }
}