package ch.sbb.scion.rcp.workbench;

import java.util.Map;

/**
 * Configures the popup to display a microfrontend in an Eclipse dialog using the {@link ISciWorkbenchPopupService}
 */
public class SciWorkbenchPopupConfig {

  /**
   * Controls the initial location of the popup. Currently only a top left anchor is supported; i.e., the top and left fields of the anchor
   * specify the location of the top left corner of the Eclipse dialog in parent shell coordinates. The parent shell is the shell of the
   * view that the popup belongs to.
   */
  public SciWorkbenchPopupOrigin anchor;

  /**
   * Allows passing data to the popup microfrontend. The popup provider can declare mandatory and optional parameters. No additional
   * parameters may be included. Refer to the documentation of the capability for more information.
   */
  public Map<String, Object> params;

  /**
   * Indicates whether the popup should close if the escape key is pressed.
   */
  public Boolean closeOnEscape;

  /**
   * Indicates whether the popup should close if it loses focus.
   */
  public Boolean closeOnFocusLost;

  /**
   * Specifies the view the popup belongs to.
   */
  public String viewId;

  public SciWorkbenchPopupConfig anchor(final SciWorkbenchPopupOrigin anchor) {
    this.anchor = anchor;
    return this;
  }

  public SciWorkbenchPopupConfig params(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public SciWorkbenchPopupConfig closeOnEscape(final Boolean closeOnEscape) {
    this.closeOnEscape = closeOnEscape;
    return this;
  }

  public SciWorkbenchPopupConfig closeOnFocusLost(final Boolean closeOnFocusLost) {
    this.closeOnFocusLost = closeOnFocusLost;
    return this;
  }

  public SciWorkbenchPopupConfig viewId(final String viewId) {
    this.viewId = viewId;
    return this;
  }

}
