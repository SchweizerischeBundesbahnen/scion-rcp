package ch.sbb.scion.rcp.workbench;

import java.util.concurrent.CompletableFuture;

import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

/**
 * Allows displaying a microfrontend in an Eclipse dialog.
 */
public interface IWorkbenchPopupService {

  /**
   * Displays a microfrontend in an Eclipse dialog based on the given qualifier.
   *
   * @param <T>
   *          The type of the result
   * @param qualifier
   *          Identifies the popup capability that provides the microfrontend for display as dialog.
   * @param config
   *          Controls popup behavior.
   * @param resultClazz
   *          The type of the result
   * @return Future that completes to the result when closed with a result, or to <code>null</code> otherwise. The future completes
   *         exceptionally if opening the popup failed, or if the popup was closed with an exception.
   */
  <T> CompletableFuture<T> open(final Qualifier qualifier, final WorkbenchPopupConfig config, final Class<T> resultClazz);

}
