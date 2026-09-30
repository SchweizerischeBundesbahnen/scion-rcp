/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench.popup;

import java.util.Objects;

import ch.sbb.scion.rcp.workbench.IReferrer;

/**
 * Refers a popup to an application.
 */
public class PopupReferrer implements IReferrer {

  private final String appSymbolicName;

  public PopupReferrer(final String appSymbolicName) {
    this.appSymbolicName = Objects.requireNonNull(appSymbolicName);
  }

  @Override
  public String getAppSymbolicName() {
    return appSymbolicName;
  }
}
