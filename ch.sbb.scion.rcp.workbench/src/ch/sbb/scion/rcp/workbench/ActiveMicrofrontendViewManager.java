/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;

import ch.sbb.scion.rcp.workbench.view.MicrofrontendViewEditorPart;

/**
 * TODO Klasse dokumentieren
 */
public class ActiveMicrofrontendViewManager {

  private Shell activeShell;
  private Widget focusedWidget;
  private IWorkbenchPart activeWorkbenchPart;
  private MicrofrontendViewEditorPart focusedMicrofrontendViewEditorPart;

  public void install() {
    var display = Display.getDefault();
    display.addFilter(SWT.Activate, event -> {
      focusedWidget = event.widget;
      if (event.widget instanceof Shell activatedShell) {
        //        Platform.getLog(getClass()).info("Activated shell=%s".formatted(activatedShell));
        activeShell = activatedShell;
      }
      logState();
    });
    display.addFilter(SWT.Deactivate, event -> {
      var log = false;
      if (focusedWidget == event.widget) {
        focusedWidget = null;
        log = true;
      }
      if (event.widget instanceof Shell deactivatedShell && activeShell == deactivatedShell) {
        //        Platform.getLog(getClass()).info("Deactivated shell=%s".formatted(deactivatedShell));
        activeShell = null;
        log = true;
      }
      if (log) {
        logState();
      }
    });
    //    display.addFilter(SWT.FocusIn, event -> {
    //      focusedWidget = event.widget;
    //      logState();
    //    });
    //    display.addFilter(SWT.FocusOut, event -> {
    //      if (focusedWidget == event.widget) {
    //        focusedWidget = null;
    //        logState();
    //      }
    //    });
  }

  public void registerFocusListener(final MicrofrontendViewEditorPart mfPart) {
    mfPart.onFocusWithin(focusWithin -> {
      if (focusWithin != null && focusWithin.booleanValue()) {
        focusedMicrofrontendViewEditorPart = mfPart;
      }
      else if (focusedMicrofrontendViewEditorPart == mfPart) {
        focusedMicrofrontendViewEditorPart = null;
      }
      logState();
    });
  }

  public void setActiveWorkbenchPart(final IWorkbenchPart workbenchPart) {
    activeWorkbenchPart = workbenchPart;
    logState();
  }

  public void unsetActiveWorkbenchPart(final IWorkbenchPart workbenchPart) {
    if (activeWorkbenchPart == workbenchPart) {
      activeWorkbenchPart = null;
    }
    logState();
  }

  public void unregisterFocusListener(final MicrofrontendViewEditorPart mfPart) {
    // TODO: Dispose listener?
  }

  private void logState() {
    var state = getState();
    Platform.getLog(getClass()).info("Activate?%s, state=%s, ".formatted(activatefocusedMicrofrontendViewEditorPart(state), state));
  }

  private boolean activatefocusedMicrofrontendViewEditorPart(final State state) {
    var activate = false;
    if (state.activeShell != null) {
      if (state.focusedWidget == null && state.focusedMfPart != null) {
        activate = true;
      }
      // TODO: Will the RouterOutlet ever be focused? (Maybe when you click on the tab?) <- Probably we don't have to handle this as there is a focusWithin event in that case anyway?!
    }
    return activate;
  }

  private State getState() {
    return new State(activeShell, focusedWidget, activeWorkbenchPart, focusedMicrofrontendViewEditorPart);
  }

  private record State(Shell activeShell, Widget focusedWidget, IWorkbenchPart activeWorkbenchPart,
      MicrofrontendViewEditorPart focusedMfPart) {

  }
}
