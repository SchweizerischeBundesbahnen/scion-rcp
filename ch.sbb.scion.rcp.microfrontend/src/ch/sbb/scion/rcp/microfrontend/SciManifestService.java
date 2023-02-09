package ch.sbb.scion.rcp.microfrontend;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.browser.RxJsObservable;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.internal.gson.GsonFactory;
import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.microfrontend.script.Script;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html"
 */
@Component(service = SciManifestService.class)
public class SciManifestService {

  private CompletableFuture<List<Application>> applications;

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#applications"
   */
  public CompletableFuture<List<Application>> getApplications() {
    if (applications == null) {
      applications = new CompletableFuture<List<Application>>();
      new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
        applications.complete(GsonFactory.create().fromJson((String) args[0], new ParameterizedType(List.class, Application.class)));
      }).installOnce().thenAccept(callback -> {
        new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser,
            Resources.readString("js/sci-manifest-service/lookup-applications.js")).replacePlaceholder("callback", callback.name)
                .replacePlaceholder("refs.ManifestService", Refs.ManifestService).replacePlaceholder("helpers.toJson", Helpers.toJson)
                .execute();
      });
    }
    return applications;
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#lookupCapabilities"
   */
  public ISubscription lookupCapabilities(final ISubscriber<List<Capability>> subscriber) {
    return lookupCapabilities(null, subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#lookupCapabilities"
   */
  public ISubscription lookupCapabilities(final ManifestObjectFilter filter, final ISubscriber<List<Capability>> listener) {
    var manifestObjectFilter = Optional.ofNullable(filter).orElse(new ManifestObjectFilter());
    var observeIIFE = new Script(Resources.readString("js/sci-manifest-service/lookup-capabilities.iife.js"))
        .replacePlaceholder("refs.ManifestService", Refs.ManifestService)
        .replacePlaceholder("filter.id", manifestObjectFilter.id, Flags.ToJson)
        .replacePlaceholder("filter.type", manifestObjectFilter.type, Flags.ToJson)
        .replacePlaceholder("filter.qualifier", manifestObjectFilter.qualifier, Flags.ToJson)
        .replacePlaceholder("filter.appSymbolicName", manifestObjectFilter.appSymbolicName, Flags.ToJson)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson).substitute();

    var observable = new RxJsObservable<List<Capability>>(microfrontendPlatformRcpHost.whenHostBrowser, observeIIFE,
        new ParameterizedType(List.class, Capability.class));
    return observable.subscribe(listener);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#registerCapability"
   */
  public CompletableFuture<String> registerCapability(final Capability capability) {
    var registered = new CompletableFuture<String>();
    new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var error = args[0];
      if (error == null) {
        registered.complete((String) args[1]);
      }
      else {
        registered.completeExceptionally(new RuntimeException((String) error));
      }
    }).installOnce().thenAccept(callback -> {
      new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser,
          Resources.readString("js/sci-manifest-service/register-capability.js")).replacePlaceholder("callback", callback.name)
              .replacePlaceholder("capability", capability, Flags.ToJson).replacePlaceholder("refs.ManifestService", Refs.ManifestService)
              .replacePlaceholder("helpers.fromJson", Helpers.fromJson).runInsideAsyncFunction().execute();
    });

    return registered;
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#unregisterCapabilities"
   */
  public CompletableFuture<Void> unregisterCapabilities() {
    return unregisterCapabilities(null);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#unregisterCapabilities"
   */
  public CompletableFuture<Void> unregisterCapabilities(final ManifestObjectFilter filter) {
    var unregistered = new CompletableFuture<Void>();
    var manifestObjectFilter = Optional.ofNullable(filter).orElse(new ManifestObjectFilter());
    new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var error = args[0];
      if (error == null) {
        unregistered.complete(null);
      }
      else {
        unregistered.completeExceptionally(new RuntimeException((String) error));
      }
    }).installOnce().thenAccept(callback -> {
      new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser,
          Resources.readString("js/sci-manifest-service/unregister-capabilities.js")).replacePlaceholder("callback", callback.name)
              .replacePlaceholder("refs.ManifestService", Refs.ManifestService)
              .replacePlaceholder("filter.id", manifestObjectFilter.id, Flags.ToJson)
              .replacePlaceholder("filter.type", manifestObjectFilter.type, Flags.ToJson)
              .replacePlaceholder("filter.qualifier", manifestObjectFilter.qualifier, Flags.ToJson)
              .replacePlaceholder("filter.appSymbolicName", manifestObjectFilter.appSymbolicName, Flags.ToJson)
              .replacePlaceholder("helpers.fromJson", Helpers.fromJson).runInsideAsyncFunction().execute();
    });

    return unregistered;
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/ManifestObjectFilter.html"
   */
  public static class ManifestObjectFilter {

    private String id;
    private String type;
    private Qualifier qualifier;
    private String appSymbolicName;

    public String getId() {
      return id;
    }

    public ManifestObjectFilter id(final String id) {
      this.id = id;
      return this;
    }

    public String getType() {
      return type;
    }

    public ManifestObjectFilter type(final String type) {
      this.type = type;
      return this;
    }

    public Qualifier getQualifier() {
      return qualifier;
    }

    public ManifestObjectFilter qualifier(final Qualifier qualifier) {
      this.qualifier = qualifier;
      return this;
    }

    public String getAppSymbolicName() {
      return appSymbolicName;
    }

    public ManifestObjectFilter appSymbolicName(final String appSymbolicName) {
      this.appSymbolicName = appSymbolicName;
      return this;
    }
  }
}
