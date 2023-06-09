package ch.sbb.scion.rcp.microfrontend;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.ManifestObjectFilter;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html">ManifestService</a>
 */
public interface ManifestService {

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#applications"
   */
  CompletableFuture<List<Application>> getApplications();

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#lookupCapabilities"
   */
  ISubscription lookupCapabilities(final ISubscriber<List<Capability>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#lookupCapabilities"
   */
  ISubscription lookupCapabilities(final ManifestObjectFilter filter, final ISubscriber<List<Capability>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#registerCapability"
   */
  CompletableFuture<String> registerCapability(final Capability capability);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#unregisterCapabilities"
   */
  CompletableFuture<Void> unregisterCapabilities();

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#unregisterCapabilities"
   */
  CompletableFuture<Void> unregisterCapabilities(final ManifestObjectFilter filter);

}
