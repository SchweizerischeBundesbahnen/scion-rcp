package ch.sbb.scion.rcp.microfrontend;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html
 */
@Component(service = SciOutletRouter.class)
public class SciOutletRouter {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate
   */
  public CompletableFuture<Void> navigate(String url) {
    return navigate(url, new NavigationOptions());
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate
   */
  public CompletableFuture<Void> navigate(String url, String outlet) {
    return navigate(url, new NavigationOptions().outlet(outlet));
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate
   */
  public CompletableFuture<Void> navigate(String url, NavigationOptions navigationOptions) {
    var options = Optional.ofNullable(navigationOptions).orElse(new NavigationOptions());

    var navigated = new CompletableFuture<Void>();
    new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var error = args[0];
      if (error == null) {
        navigated.complete(null);
      }
      else {
        navigated.completeExceptionally(new RuntimeException((String) error));
      }
    })
        .installOnce()
        .thenAccept(callback -> {
          new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser, """
              try {
                await ${refs.OutletRouter}.navigate(${helpers.fromJson}('${url}') ?? undefined, {
                  outlet: ${helpers.fromJson}('${options.outlet}') ?? undefined,
                  relativeTo: ${helpers.fromJson}('${options.relativeTo}') ?? undefined,
                  params: ${helpers.fromJson}('${options.params}') ?? undefined,
                  pushStateToSessionHistoryStack: ${options.pushStateToSessionHistoryStack} ?? undefined,
                });
                window['${callback}'](null);
              }
              catch (error) {
                window['${callback}'](error.message || `${error}` || 'ERROR');
              }
              """)
              .replacePlaceholder("callback", callback.name)
              .replacePlaceholder("url", url, Flags.ToJson)
              .replacePlaceholder("options.outlet", options.outlet, Flags.ToJson)
              .replacePlaceholder("options.relativeTo", options.relativeTo, Flags.ToJson)
              .replacePlaceholder("options.params", options.params, Flags.ToJson)
              .replacePlaceholder("options.pushStateToSessionHistoryStack", options.pushStateToSessionHistoryStack)
              .replacePlaceholder("refs.OutletRouter", Refs.OutletRouter)
              .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
              .runInsideAsyncFunction()
              .execute();
        });

    return navigated;
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/NavigationOptions.html
   */
  public static class NavigationOptions {
    private String outlet;
    private String relativeTo;
    private Map<String, Object> params;
    private Boolean pushStateToSessionHistoryStack;

    public NavigationOptions outlet(String outlet) {
      this.outlet = outlet;
      return this;
    }

    public NavigationOptions relativeTo(String relativeTo) {
      this.relativeTo = relativeTo;
      return this;
    }

    public NavigationOptions params(Map<String, Object> params) {
      this.params = params;
      return this;
    }

    public NavigationOptions pushStateToSessionHistoryStack(Boolean pushStateToSessionHistoryStack) {
      this.pushStateToSessionHistoryStack = pushStateToSessionHistoryStack;
      return this;
    }
  }
}
