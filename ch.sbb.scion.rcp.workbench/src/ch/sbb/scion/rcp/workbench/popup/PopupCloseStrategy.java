package ch.sbb.scion.rcp.workbench.popup;

public class PopupCloseStrategy {

  public Boolean onEscape;

  public Boolean onFocusLost;

  public PopupCloseStrategy onEscape(final Boolean onEscape) {
    this.onEscape = onEscape;
    return this;
  }

  public PopupCloseStrategy onFocusLost(final Boolean onFocusLost) {
    this.onFocusLost = onFocusLost;
    return this;
  }

}
