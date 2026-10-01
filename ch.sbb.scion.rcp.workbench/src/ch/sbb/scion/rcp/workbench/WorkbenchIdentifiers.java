/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

import java.util.UUID;

/**
 * Utility for generating and checking identifiers used for identifying workbench components like views or popups.
 */
public final class WorkbenchIdentifiers {

  private static final String PREFIX_DELIMITER = ".";
  private static final String VIEW_ID_PREFIX = "view";
  private static final String POPUP_ID_PREFIX = "popup";

  public static String viewId() {
    return newId(VIEW_ID_PREFIX);
  }

  public static String popupId() {
    return newId(POPUP_ID_PREFIX);
  }

  private static String newId(final String prefix) {
    // We use a truncated UUID as suffix, identical to the scion-workbench-client implementation:
    return prefix + PREFIX_DELIMITER + UUID.randomUUID().toString().substring(0, 8);
  }

  private WorkbenchIdentifiers() {
    // utility
  }
}
