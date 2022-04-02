package ch.sbb.scion.rcp.microfrontend;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback.Options;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
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
  public CompletableFuture<Void> navigate(String url, NavigationOptions options) {
    options = Optional.ofNullable(options).orElse(new NavigationOptions());

    var navigated = new CompletableFuture<Void>();
    var callback = new BrowserCallback(microfrontendPlatformRcpHost.whenHostBrowser, new Options()
        .once()
        .onCallback(args -> {
          var error = args[0];
          if (error == null) {
            navigated.complete(null);
          }
          else {
            navigated.completeExceptionally(new RuntimeException((String) error));
          }
        }));

    new BrowserScriptExecutor(microfrontendPlatformRcpHost.whenHostBrowser, """
        try {
          await ${refs.OutletRouter}.navigate(JSON.parse('${url}') ?? undefined, {
            outlet: JSON.parse('${options.outlet}') ?? undefined,
            relativeTo: JSON.parse('${options.relativeTo}') ?? undefined,
            params: JSON.parse('${options.params}') ?? undefined,
            pushStateToSessionHistoryStack: ${options.pushStateToSessionHistoryStack} ?? undefined,
          });
          window['${callback}'](null);
        }
        catch (error) {
          window['${callback}'](error.message ?? `${error}` ?? 'ERROR');
        }
        """)
        .replacePlaceholder("callback", callback)
        .replacePlaceholder("url", url, Flags.ToJson)
        .replacePlaceholder("options.outlet", options.outlet, Flags.ToJson)
        .replacePlaceholder("options.relativeTo", options.relativeTo, Flags.ToJson)
        .replacePlaceholder("options.params", options.params, Flags.ToJson)
        .replacePlaceholder("options.pushStateToSessionHistoryStack", options.pushStateToSessionHistoryStack)
        .replacePlaceholder("refs.OutletRouter", Refs.OutletRouter)
        .runInsideAsyncFunction()
        .execute();

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
