package ch.sbb.scion.rcp.microfrontend;

import java.util.concurrent.CompletableFuture;

import ch.sbb.scion.rcp.microfrontend.model.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html">OutletRouter</a>
 */
public interface OutletRouter {

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  CompletableFuture<Void> navigate(final String url);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  CompletableFuture<Void> navigate(final String url, final NavigationOptions navigationOptions);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  CompletableFuture<Void> navigate(final Qualifier qualifier);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/OutletRouter.html#navigate"
   */
  CompletableFuture<Void> navigate(final Qualifier qualifier, final NavigationOptions navigationOptions);

}
