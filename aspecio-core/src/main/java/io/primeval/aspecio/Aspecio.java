package io.primeval.aspecio;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import io.primeval.reflex.proxy.Interceptor;


/**
 * <p>
 * <b>Aspecio Service Interface</b><br>
 * 
 * (mainly interesting for building a UI to get some information about what Aspecio is doing)
 * </p>
 * 
 * <p>
 * It is not possible to change the state of Aspecio through this class. It is merely a read-only view.
 * </p>
 * 
 * <p>
 * To define and update aspects, a service object implementing {@link Interceptor} must be registered to the OSGi
 * service registry using {@link BundleContext#registerService(Class, Object, java.util.Dictionary)} with the
 * {@link String} property {@link AspecioConstants#SERVICE_ASPECT} set to the name of the aspect.<br>
 * An aspect service may define the property {@link AspecioConstants#SERVICE_ASPECT_EXTRAPROPERTIES} of type
 * {@link String} or String[] to have one or several extra OSGi properties registered with the services it is woven
 * into. For instance, if an aspect defines an extraProperty named {@code secure}, then services woven with that aspect
 * will be published with the OSGi property {@code secure} and value {@code true}. This can in turn be used to make
 * sure, from the consuming code, that a service exposes a certain behavior (here, that it is indeed secured, whether by
 * the aspect or by custom code).
 * </p>
 * <p>
 * Aspecio will <i>weave</i> aspects into OSGi services that define the service properties
 * {@link AspecioConstants#SERVICE_ASPECT_WEAVE} (for required aspects) or
 * {@link AspecioConstants#SERVICE_ASPECT_WEAVE_OPTIONAL}.<br>
 * By default, if a required aspect is not present, then the original service will not be available until a service
 * providing the required aspect is registered. To change that behavior and disable the filtering of services, you may
 * use the framework property {@link AspecioConstants#ASPECIO_FILTER_SERVICES} and set it to {@code false}.<br>
 * Changing that property using Java system properties will only be taken into account after restarting Aspecio's
 * bundle. Note that due to the impossibility to publish a previously filtered service, you should restart bundles
 * providing woven services as well after changing that property.
 * </p>
 * 
 * @author Simon Chemouil
 */
public interface Aspecio {

    /**
     * <p>
     * Get the set of Aspects currently registered as seen by Aspecio.
     * </p>
     * <p>
     * The returned set is a view and changing it will not change the state of Aspecio.
     * </p>
     * 
     * @return The set containing the registered Aspect names.
     */
    Set<String> getRegisteredAspects();

    /**
     * <p>
     * Get the {@link AspectDescription} for Aspect named {@literal aspecName}, or {@link Optional#empty()} if there is
     * no such Aspect.
     * </p>
     * 
     * @param aspectName
     *            The name of the aspect (case sensitive)
     * @return An Optional containing the matching {@link AspectDescription}, or {@link Optional#empty()}
     */
    Optional<AspectDescription> getAspectDescription(String aspectName);

    /**
     * <p>
     * Get the list of {@link InterceptedServiceDescription}, as seen by Aspecio.
     * </p>
     * 
     * @return The list of {@link InterceptedServiceDescription}, or an empty list if there are no intercepted services.
     */
    List<InterceptedServiceDescription> getInterceptedServices();

    /**
     * <p>
     * Get the list of {@link InterceptedServiceDescription}, as seen by Aspecio, filtered by objectClass.
     * </p>
     * 
     * @param objectClassContains
     *            A filter that must be part of the {@link Constants#OBJECTCLASS} OSGi property of the intercepted
     *            service to be selected.
     * @return The list of {@link InterceptedServiceDescription}, or an empty list if there are no intercepted services.
     */
    default List<InterceptedServiceDescription> getInterceptedServices(String objectClassContains) {
        List<InterceptedServiceDescription> interceptedServices = getInterceptedServices();
        Iterator<InterceptedServiceDescription> iterator = interceptedServices.iterator();
        entryLoop: while (iterator.hasNext()) {
            InterceptedServiceDescription serviceDescription = iterator.next();
            for (String objClass : serviceDescription.objectClass) {
                if (objClass.contains(objectClassContains)) {
                    continue entryLoop;
                }
            }
            iterator.remove();
        }
        return interceptedServices;
    }

}
