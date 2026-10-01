/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench.internal;

/**
 * Defines command endpoints for the communication between SCION Workbench and SCION Workbench Client.
 */
public final class WorkbenchCommands {

  public static String popupOriginTopic(final String popupId) {
    return String.format("ɵworkbench/popups/%s/origin", popupId);
  }

  public static String popupFocusedTopic(final String popupId) {
    return String.format("ɵworkbench/popups/%s/focused", popupId);
  }

  public static String popupCloseTopic(final String popupId) {
    return String.format("ɵworkbench/popups/%s/close", popupId);
  }

  public static String popupResultTopic(final String popupId) {
    return String.format("ɵworkbench/popups/%s/result", popupId);
  }

  public static String viewParamsTopic(final String viewId) {
    return String.format("ɵworkbench/views/%s/params", viewId);
  }

  public static String viewActiveTopic(final String viewId) {
    return String.format("ɵworkbench/views/%s/active", viewId);
  }

  public static String viewUnloadingTopic(final String viewId) {
    return String.format("ɵworkbench/views/%s/unloading", viewId);
  }

  public static String viewTitleTopic(final String viewId) {
    return String.format("ɵworkbench/views/%s/title", viewId);
  }

  public static String viewDirtyTopic(final String viewId) {
    return String.format("ɵworkbench/views/%s/dirty", viewId);
  }

  public static String viewHeadingTopic(final String viewId) {
    return String.format("ɵworkbench/views/%s/heading", viewId);
  }

  private WorkbenchCommands() {
    // utility
  }
}
