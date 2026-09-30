package ch.sbb.scion.rcp.workbench.popup;

import ch.sbb.scion.rcp.workbench.WorkbenchIdentifiers;

public class PopupCommand {

  public String popupId;

  public String context;

  public PopupCloseStrategy closeStrategy;

  // todo: add cssClass?

  public PopupCommand() {
    this.popupId = WorkbenchIdentifiers.popupId();
  }

  public PopupCommand context(final String context) {
    this.context = context;
    return this;
  }

  public PopupCommand closeStrategy(final PopupCloseStrategy closeStrategy) {
    this.closeStrategy = closeStrategy;
    return this;
  }

}
