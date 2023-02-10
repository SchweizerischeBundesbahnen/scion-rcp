package ch.sbb.scion.rcp.microfrontend.e3.app.demo.popup;

import org.eclipse.swt.widgets.Shell;

import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopup;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopupWindow;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopupWindowProvider;

public class TestPopupDialogProvider implements ISciWorkbenchPopupWindowProvider {

  @Override
  public ISciWorkbenchPopupWindow create(final Shell parentShell, final ISciWorkbenchPopup popup) {
    return new TestPopupDialog(parentShell, popup);
  }

}
