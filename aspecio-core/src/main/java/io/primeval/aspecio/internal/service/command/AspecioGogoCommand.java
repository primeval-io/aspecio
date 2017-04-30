package io.primeval.aspecio.internal.service.command;

import java.util.List;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import io.primeval.aspecio.Aspecio;
import io.primeval.aspecio.AspectDescription;
import io.primeval.aspecio.InterceptedServiceDescription;
import io.primeval.aspecio.InterceptorDescription;

// This sysout print stuff is ugly, but that's how Felix Gogo works
// by redirecting System.out in the calling thread to the proper shell...
// (in exchange, we don't have to declare any extra dependency!)
// Those methods are found by reflection, and declared as properties when we register the service
public final class AspecioGogoCommand {

    /* module */ public static final String ASPECIO_GOGO_COMMAND_SCOPE = "aspect";
    /* module */ public static final String[] ASPECIO_GOGO_COMMANDS = new String[] { "aspects", "woven" };

    private final BundleContext bundleContext;
    private final Aspecio aspecio;

    public AspecioGogoCommand(BundleContext bundleContext, Aspecio aspecio) {
        this.bundleContext = bundleContext;
        this.aspecio = aspecio;
    }

    // Gogo command "aspect:aspects"
    public void aspects() {
        for (String aspectName : aspecio.getRegisteredAspects()) {
            System.out.println("* " + aspectName);
            Optional<AspectDescription> aspectDescription = aspecio.getAspectDescription(aspectName);
            if (!aspectDescription.isPresent()) {
                System.out.println(" ...?! Err, that aspect just went away!");
                continue;
            }
            AspectDescription description = aspectDescription.get();
            printInterceptorDescription("[ --- active --- ]", description.interceptor);
            int i = 1;
            for (InterceptorDescription id : description.backupInterceptors) {
                printInterceptorDescription("[ alternative #" + i + " ]", id);
                i++;
            }
        }
    }

    private void printInterceptorDescription(String marker, InterceptorDescription interceptorDescription) {
        String shift = String.format("%" + (marker.length() + 3) + "s", "");
        System.out.println("  " + marker + " Service id " + interceptorDescription.serviceId + ", class "
                + interceptorDescription.interceptorClass.getName() + ", extra properties: " + interceptorDescription.extraProperties);
        long serviceBundleId = interceptorDescription.bundleId;
        Bundle bundle = bundleContext.getBundle(serviceBundleId);
        System.out
                .println(shift + "Provided by: " + bundle.getSymbolicName() + " " + bundle.getVersion() + " [" + serviceBundleId
                        + "]");
    }

    // Gogo command "aspect:woven"
    public void woven() {
        List<InterceptedServiceDescription> interceptedServices = aspecio.getInterceptedServices();
        printWoven(interceptedServices);
    }

    public void woven(String objectClassContains) {
        List<InterceptedServiceDescription> interceptedServices = aspecio.getInterceptedServices(objectClassContains);
        printWoven(interceptedServices);
    }

    private void printWoven(List<InterceptedServiceDescription> interceptedServices) {
        int i = 0;
        int shiftSize = 4 + (interceptedServices.size() / 10);
        String shift = String.format("%" + shiftSize + "s", "");
        for (InterceptedServiceDescription mws : interceptedServices) {
            System.out.println(
                    "[" + i + "] Service id: " + mws.serviceId + ", objectClass: " + mws.objectClass);
            System.out.println(shift + "Required aspects: " + mws.requiredAspects +
                    ", Optional aspects: " + mws.optionalAspects);
            long serviceBundleId = mws.bundleId;
            Bundle bundle = bundleContext.getBundle(serviceBundleId);
            System.out
                    .println(shift + "Provided by: " + bundle.getSymbolicName() + " " + bundle.getVersion() + " [" + serviceBundleId + "]");
            boolean satisfied = mws.published;
            System.out.println(shift + "Satisfied: " + satisfied);
            if (!satisfied) {
                System.out.println(shift + "Missing required aspects: " + mws.unsatisfiedRequiredAspects);
            } else {
                System.out.println(shift + "Active aspects: " + mws.satisfiedAspects);
            }
            i++;
        }
    }

}
