package io.primeval.aspecio.it;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;

import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

public final class TestProvisioningConfig {
    public static Option baseOptions() {
        return composite(bootDelegationPackage("sun.*"), cleanCaches(),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.tracker.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.exam.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.extender.service.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.base.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.core.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.extender.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.lifecycle.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.apache.geronimo.specs.atinject.link").startLevel(START_LEVEL_SYSTEM_BUNDLES));
    }

    public static Option testingBundles() {
        return composite(junitBundles(), mavenBundle("org.assertj", "assertj-core").versionAsInProject());
    }

    public static Option slf4jLogging() {
        return composite(systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() +
                "/src/test/resources/logback.xml"),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject().startLevel(Constants.START_LEVEL_SYSTEM_BUNDLES),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject().startLevel(Constants.START_LEVEL_SYSTEM_BUNDLES));
    }

    public static Option dsAndFriends() {
        return composite(mavenBundle("org.apache.felix", "org.apache.felix.log", "1.0.1"),
                mavenBundle("org.apache.felix", "org.apache.felix.metatype", "1.2.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.9.2"),
                mavenBundle("org.apache.felix", "org.apache.felix.scr", "2.1.0"));
    }

}
