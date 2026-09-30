/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

/**
 * Identifies the source context from which a workbench element was opened.
 */
public interface IReferrer {

  /**
   * @return the symbolic name of the referring application
   */
  String getAppSymbolicName();

}
