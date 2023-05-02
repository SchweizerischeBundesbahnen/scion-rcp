/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench.view;

import java.util.Objects;

import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.workbench.IWorkbenchViewInput;

public class ViewPartInput implements IWorkbenchViewInput {

  private Intent intent;

  public ViewPartInput intent(final Intent intent) {
    Objects.requireNonNull(intent);
    this.intent = intent;
    return this;
  }

  @Override
  public Intent getIntent() {
    return intent;
  }

}
