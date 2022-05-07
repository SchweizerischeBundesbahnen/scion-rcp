package ch.sbb.scion.rcp.microfrontend;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.internal.GsonFactory;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html
 */
@Component(service = SciManifestService.class)
public class SciManifestService {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/ManifestService.html#applications
   */
  public CompletableFuture<List<Application>> getApplications() {
    var applications = new CompletableFuture<List<Application>>();
    new JavaScriptCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      applications.complete(GsonFactory.create().fromJson((String) args[0], new ParameterizedType(List.class, Application.class)));
    })
        .installOnce()
        .thenAccept(callback -> {
          new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser, """
              const applications = ${refs.ManifestService}.applications;
              window['${callback}'](${helpers.toJson}(applications));
              """)
              .replacePlaceholder("callback", callback.name)
              .replacePlaceholder("refs.ManifestService", Refs.ManifestService)
              .replacePlaceholder("helpers.toJson", Helpers.toJson)
              .execute();
        });

    return applications;
  }
}
