/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

import ch.sbb.scion.rcp.microfrontend.model.Intent;

/**
 * Allows injecting intent information into an Eclipse IViewPart that is opened through a Scion workbench view intent. An instance of this
 * interface will be available in the dependency injection context of the workbench window that contains the view part.
 */
public interface ISciWorkbenchViewInput {

  /**
   * @return the intent that was issued to open the view that this input belongs to, never null
   */
  Intent getIntent();

}
