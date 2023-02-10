package ch.sbb.scion.rcp.microfrontend.e3.app.demo.popup;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.google.gson.GsonBuilder;

import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopup;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopupWindow;

public class TestPopupDialog extends Dialog implements ISciWorkbenchPopupWindow {

  private static final int CLOSE_WITH_EXCEPTION_ID = IDialogConstants.CLIENT_ID + 1;

  private final ISciWorkbenchPopup popup;

  private boolean activated = false;

  protected TestPopupDialog(final Shell parentShell, final ISciWorkbenchPopup popup) {
    super(parentShell);
    this.popup = popup;

    configureShellStyle();
  }

  @Override
  protected Control createDialogArea(final Composite parent) {
    var area = (Composite) super.createDialogArea(parent);
    var gson = new GsonBuilder().setPrettyPrinting().create();

    LabelFactory.newLabel(SWT.NONE).text("Capability:").create(area);
    var capabilityText = TextFactory.newText(SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL).create(area);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(capabilityText);
    capabilityText.setText(gson.toJson(popup.getCapability()));

    LabelFactory.newLabel(SWT.NONE).text("Parameters:").create(area);
    var paramsText = TextFactory.newText(SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL).create(area);
    GridDataFactory.fillDefaults().grab(true, false).hint(new Point(SWT.DEFAULT, 100)).applyTo(paramsText);
    paramsText.setText(gson.toJson(popup.getParams()));

    LabelFactory.newLabel(SWT.NONE).text("Referrer View ID:").create(area);
    var referrerText = TextFactory.newText(SWT.BORDER | SWT.READ_ONLY).create(area);
    GridDataFactory.fillDefaults().grab(true, false).applyTo(referrerText);
    referrerText.setText(popup.getReferrerViewId().orElse("-"));

    var closeOnEscape = popup.closeOnEscape();
    area.addTraverseListener(event -> {
      if (!closeOnEscape && event.detail == SWT.TRAVERSE_ESCAPE) {
        // Prevent default; i.e., closing the dialog:
        event.doit = true;
        event.detail = SWT.TRAVERSE_NONE;
      }
    });

    return area;
  }

  @Override
  protected void okPressed() {
    popup.close("OK");
    super.okPressed();
  }

  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);

    var closeWithExceptionButton = createButton(parent, CLOSE_WITH_EXCEPTION_ID, "Close with exception", false);
    closeWithExceptionButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        popup.closeWithException(new RuntimeException("Closed with exception!"));
      }

    });
  }

  @Override
  protected Point getInitialSize() {
    return popup.getInitialSize().orElse(super.getInitialSize());
  }

  @Override
  protected void configureShell(final Shell newShell) {
    super.configureShell(newShell);
    var properties = popup.getCapability().properties;
    newShell.setText(properties.has("title") ? properties.get("title") : "");

    var closeOnFocusLost = popup.closeOnFocusLost();
    newShell.addListener(SWT.Deactivate, event -> {
      if (activated && closeOnFocusLost) {
        popup.close(null);
      }
    });
    newShell.addListener(SWT.Activate, event -> activated = true);
  }

  private void configureShellStyle() {
    var style = getShellStyle();
    // Not resizable:
    style = style & ~SWT.RESIZE;
    // Not modal:
    style = style & ~SWT.APPLICATION_MODAL;
    setShellStyle(style);
  }

  @Override
  public void init() {
    // Do nothing
  }

}
