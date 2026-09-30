/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Utility for generating and checking identifiers used for identifying workbench components like views or popups.
 */
public final class WorkbenchIdentifiers {

  private static final String PREFIX_DELIMITER = ".";
  private static final String VIEW_ID_PREFIX = "view";
  private static final String POPUP_ID_PREFIX = "popup";
  private static final Set<String> PREFIXES = Set.of(VIEW_ID_PREFIX, POPUP_ID_PREFIX);

  public static boolean isWorkbenchIdentifier(final String identifier) {
    Objects.requireNonNull(identifier);
    return identifier.contains(PREFIX_DELIMITER) && PREFIXES.contains(identifier.split(PREFIX_DELIMITER)[0]);
  }

  public static String viewId() {
    return VIEW_ID_PREFIX + PREFIX_DELIMITER + truncatedUUID();
  }

  public static String popupId() {
    return POPUP_ID_PREFIX + PREFIX_DELIMITER + truncatedUUID();
  }

  private static String truncatedUUID() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  private WorkbenchIdentifiers() {
    // utility
  }
}
