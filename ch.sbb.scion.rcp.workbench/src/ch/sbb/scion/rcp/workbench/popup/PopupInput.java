package ch.sbb.scion.rcp.workbench.popup;

import java.util.Map;

import ch.sbb.scion.rcp.microfrontend.model.Capability;

/**
 * Information about the popup embedding a microfrontend. If the microfrontend is loaded in a workbench popup then this object can be
 * obtained from the ContextService using the name ɵPOPUP_CONTEXT.
 */
public class PopupInput {

  public String popupId;

  public Map<String, Object> params;

  public Capability capability;

  public PopupReferrer referrer;

  public boolean closeOnFocusLost = false;

  public PopupInput popupId(final String popupId) {
    this.popupId = popupId;
    return this;
  }

  public PopupInput params(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public PopupInput capability(final Capability capability) {
    this.capability = capability;
    return this;
  }

  public PopupInput referrer(final PopupReferrer referrer) {
    this.referrer = referrer;
    return this;
  }

  public PopupInput closeOnFocusLost(final boolean closeOnFocusLost) {
    this.closeOnFocusLost = closeOnFocusLost;
    return this;
  }

}
