package ch.sbb.scion.rcp.workbench;

import java.util.Map;
import java.util.Optional;

import org.eclipse.swt.graphics.Point;

import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

/**
 * Provides means to interact with the RCP Scion Workbench Popup capability implementation. Dialogs that are intended to behave like Scion
 * Workbench Popups should get an instance of this interface to implement the Scion Workbench Popup functionalities. An instance of this
 * interface will be passed to the {@link IWorkbenchPopupWindowProvider} by the RCP Scion Workbench host.
 */
public interface IWorkbenchPopup {

  /**
   * @return the unique id of this popup, never null
   */
  String getPopupId();

  /**
   * @return the capability that was used to open this popup, never null
   */
  Capability getCapability();

  /**
   * @return a dictionary of parameters comprising the parameter and the qualifier key-value pairs that were included in the intention that
   *         was issued by the popup requester, never null or empty, contains at least one qualifier
   */
  Map<String, Object> getParams();

  /**
   * @return the initial size as a Point(x, y) where x = width, y = height in pixels, optional
   */
  Optional<Point> getInitialSize();

  /**
   * @return a boolean that indicates whether the popup should close if the escape key is pressed
   */
  boolean closeOnEscape();

  /**
   * @return a boolean that indicates whether the popup should close if it loses focus
   */
  boolean closeOnFocusLost();

  /**
   * @return the id of the view that this popup belongs to, optional
   */
  Optional<String> getReferrerViewId();

  /**
   * Allows observing the popup origin which is a union of coordinates relative to the parent shell.
   *
   * @return a subscription that can be used to unsubscribe from the popup origin topic
   */
  ISubscription observePopupOrigin(ISubscriber<WorkbenchPopupOrigin> subscriber);

  /**
   * Closes the popup and replies to the popup request with the provided result. The result may be null.
   */
  void close(Object result);

  /**
   * Closes the popup and replies to the popup request with an exception message based on the provided exception.
   *
   * @param ex
   *          the exception
   * @throws NullPointerException
   *           if ex is null
   */
  void closeWithException(Exception ex);

}
