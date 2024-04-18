/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

import org.eclipse.ui.IWorkbenchPart;

import ch.sbb.scion.rcp.microfrontend.IDisposable;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet.FocusWithinListener;

/**
 * Provides means to interact with the RCP Scion Workbench View capability implementation. Workbench parts that are intended to behave like
 * an RCP Scion Workbench View should implement this interface.
 */
public interface IMicrofrontendViewPart extends IWorkbenchPart {

  /**
   * <code>ID</code>: The ID that will be assigned to every workbench part of this type. Can be used to check whether a workbench part
   * reference represents an RCP Scion Workbench View, for example.
   */
  String ID = "ch.sbb.scion.rcp.workbench.editors.MicrofrontendViewEditor";

  /**
   * @return the unique ID of this RCP Scion Workbench View.
   */
  String getViewId();

  /**
   * Allows listening for focus-within events. A focus-within event is emitted when the embedded Scion Router Outlet, or any descendant,
   * gains or loses focus. In particular, the listener will receive a boolean value that is true if focus is gained, otherwise false.
   *
   * @param listener
   *          the listener that will receive the focus-within events, not null
   * @return a disposable that can be used to dispose the listener registration
   */
  IDisposable onFocusWithin(FocusWithinListener listener);
}
