package ch.sbb.scion.rcp.microfrontend;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.CapabilityIdentifier;
import ch.sbb.scion.rcp.microfrontend.model.Intention;
import ch.sbb.scion.rcp.microfrontend.model.ManifestObjectFilter;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html">ManifestService</a>
 */
public interface ManifestService {

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#applications"
   */
  CompletableFuture<List<Application>> getApplications();

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#lookupCapabilities_"
   */
  ISubscription lookupCapabilities(final ISubscriber<List<Capability>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#lookupCapabilities_"
   */
  ISubscription lookupCapabilities(final ManifestObjectFilter filter, final ISubscriber<List<Capability>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#registerCapability"
   */
  CompletableFuture<String> registerCapability(final Capability capability);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#unregisterCapabilities"
   */
  CompletableFuture<Void> unregisterCapabilities();

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#unregisterCapabilities"
   */
  CompletableFuture<Void> unregisterCapabilities(final ManifestObjectFilter filter);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#lookupIntentions_"
   */
  ISubscription lookupIntentions(final ISubscriber<List<Intention>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#lookupIntentions_"
   */
  ISubscription lookupIntentions(final ManifestObjectFilter filter, final ISubscriber<List<Intention>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#registerIntention"
   */
  CompletableFuture<String> registerIntention(final Intention intention);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#unregisterIntentions"
   */
  CompletableFuture<Void> unregisterIntentions();

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#unregisterIntentions"
   */
  CompletableFuture<Void> unregisterIntentions(final ManifestObjectFilter filter);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/ManifestService.html#isApplicationQualified_"
   */
  ISubscription isApplicationQualified(final String appSymbolicName, final CapabilityIdentifier qualifiedFor,
      final ISubscriber<Boolean> subscriber);
}
