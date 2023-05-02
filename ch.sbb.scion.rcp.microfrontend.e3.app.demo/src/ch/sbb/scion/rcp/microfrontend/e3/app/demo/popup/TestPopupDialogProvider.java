package ch.sbb.scion.rcp.microfrontend.e3.app.demo.popup;

import org.eclipse.swt.widgets.Shell;

import ch.sbb.scion.rcp.workbench.IWorkbenchPopup;
import ch.sbb.scion.rcp.workbench.IWorkbenchPopupWindow;
import ch.sbb.scion.rcp.workbench.IWorkbenchPopupWindowProvider;

public class TestPopupDialogProvider implements IWorkbenchPopupWindowProvider {

  @Override
  public IWorkbenchPopupWindow create(final Shell parentShell, final IWorkbenchPopup popup) {
    return new TestPopupDialog(parentShell, popup);
  }

}
