package ch.sbb.scion.rcp.microfrontend;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html">OutletRouter</a>
 */
@Component(service = OutletRouter.class)
public class OutletRouter {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  public CompletableFuture<Void> navigate(final String url) {
    return navigateInternal(url, null);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  public CompletableFuture<Void> navigate(final String url, final NavigationOptions navigationOptions) {
    return navigateInternal(url, navigationOptions);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  public CompletableFuture<Void> navigate(final Qualifier qualifier) {
    return navigateInternal(qualifier, null);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  public CompletableFuture<Void> navigate(final Qualifier qualifier, final NavigationOptions navigationOptions) {
    return navigateInternal(qualifier, navigationOptions);
  }

  private CompletableFuture<Void> navigateInternal(final Object target, final NavigationOptions navigationOptions) {
    Objects.requireNonNull(target);
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
    }).installOnce().thenAccept(callback -> {
      new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser, Resources.readString("js/sci-outlet-router/navigate.js"))
          .replacePlaceholder("callback", callback.name).replacePlaceholder("target", target, Flags.ToJson)
          .replacePlaceholder("options.outlet", options.outlet, Flags.ToJson)
          .replacePlaceholder("options.relativeTo", options.relativeTo, Flags.ToJson)
          .replacePlaceholder("options.params", options.params, Flags.ToJson)
          .replacePlaceholder("options.pushStateToSessionHistoryStack", options.pushStateToSessionHistoryStack)
          .replacePlaceholder("refs.OutletRouter", Refs.OutletRouter).replacePlaceholder("helpers.fromJson", Helpers.fromJson)
          .runInsideAsyncFunction().execute();
    });

    return navigated;
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/NavigationOptions.html"
   */
  @Accessors(fluent = true)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder
  @ToString
  public static class NavigationOptions {

    private String outlet;
    private String relativeTo;
    private Map<String, ?> params;
    private Boolean pushStateToSessionHistoryStack;

  }
}
