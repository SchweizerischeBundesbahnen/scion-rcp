package ch.sbb.scion.rcp.workbench;

import org.eclipse.swt.widgets.Shell;

/**
 * Allows registering Windows and Dialogs in the Eclipse context via the extension point "ch.sbb.scion.rcp.workbench.popupWindowProviders".
 * Classes that implement this interface must possess a public zero-argument constructor as the extension point logic expects to find a
 * public zero-argument constructor for creating an instance of such a class.
 */
public interface IWorkbenchPopupWindowProvider {

  /**
   * Factory method for creating a new Window of type T. This method is used by the RCP Workbench host when a Popup intent that requests the
   * provided Window is intercepted.
   *
   * @param parentShell
   *          the parent shell of the Window, may be null
   * @param popup
   *          a proxy for interacting with the RCP Scion Workbench Popup capability implementation
   * @return a new Window of type T that should be available as a Scion Workbench Popup
   */
  IWorkbenchPopupWindow create(Shell parentShell, IWorkbenchPopup popup);

}
