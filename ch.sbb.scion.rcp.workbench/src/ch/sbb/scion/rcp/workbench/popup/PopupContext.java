/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench.popup;

import java.util.Map;

import ch.sbb.scion.rcp.microfrontend.model.Capability;

/**
 * Context when displaying a microfrontend in a popup.
 */
public class PopupContext {

  public static final String POPUP_CONTEXT = "ɵworkbench.popup";

  public String popupId;

  public Map<String, Object> params;

  public Capability capability;

  public PopupReferrer referrer;

  public PopupContext popupId(final String popupId) {
    this.popupId = popupId;
    return this;
  }

  public PopupContext params(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public PopupContext capability(final Capability capability) {
    this.capability = capability;
    return this;
  }

  public PopupContext referrer(final PopupReferrer referrer) {
    this.referrer = referrer;
    return this;
  }
}
