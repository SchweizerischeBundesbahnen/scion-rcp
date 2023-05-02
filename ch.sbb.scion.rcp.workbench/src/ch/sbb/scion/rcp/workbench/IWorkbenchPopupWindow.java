package ch.sbb.scion.rcp.workbench;

import org.eclipse.jface.window.IShellProvider;

/**
 * Defines the minimal requirements for an RCP Scion Workbench Popup window. Intended to be implemented by Dialogs that should be opened
 * using the Scion Workbench Popup capability.
 */
public interface IWorkbenchPopupWindow extends IShellProvider {

  /**
   * Place any potentially long running initialization tasks in this method to not block the intent handling. Called before
   * {@link IWorkbenchPopupWindow#open()}.
   */
  void init();

  /**
   * Opens this popup window without blocking; i.e., this method should not wait for the user to close the popup but return immediately
   * after creating the popup contents and making it visible in the UI. If the implementation of this interface allows blocking on open,
   * then this behavior should be configurable via {@link IWorkbenchPopupWindow#setBlockOnOpen(boolean)}.<br>
   * <br>
   * This method should create the shell of this popup window. Therefore, the shell should be accessible via
   * {@link IWorkbenchPopupWindow#getShell()} after this method has been called.
   *
   * @return an implementation-specific return code
   */
  int open();

  /**
   * Sets whether the <code>open</code> method should block until the popup window closes. A popup window is expected to not block on open,
   * by default.
   *
   * @param shouldBlock
   *          <code>true</code> if the <code>open</code> method should not return until the popup window closes, and <code>false</code> if
   *          the <code>open</code> method should return immediately
   */
  void setBlockOnOpen(boolean shouldBlock);

  /**
   * Closes this popup window and disposes its shell.
   *
   * @return <code>true</code> if the popup window is (or was already) closed, and <code>false</code> if it is still open
   */
  boolean close();

}
